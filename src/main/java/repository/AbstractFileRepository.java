package repository;

import domain.Entity;
import domain.Validator;
import exceptions.RepositoryException;
import exceptions.ValidationException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;

public abstract class AbstractFileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID, E> {
    private final Path file;

    protected AbstractFileRepository(Validator<E> validator, String fileName) {
        super(validator);
        this.file = Path.of(fileName);
        loadData();
    }

    protected abstract E extractEntity(String line);

    protected abstract String createEntityAsString(E entity);

    private void loadData() {
        if (!Files.exists(file)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (!line.isBlank()) {
                    loadLine(line, lineNumber);
                }
            }
        } catch (IOException e) {
            throw new RepositoryException("Nu pot citi fisierul " + file + ": " + e.getMessage(), e);
        }
    }

    private void loadLine(String line, int lineNumber) {
        String where = file + ", linia " + lineNumber + " (\"" + line + "\")";
        E entity;
        try {
            entity = extractEntity(line);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new RepositoryException(where + ": format invalid - " + e.getMessage(), e);
        }
        try {
            if (super.save(entity) != null) {
                throw new RepositoryException(where + ": id duplicat " + entity.getId());
            }
        } catch (ValidationException e) {
            throw new RepositoryException(where + ": date invalide - " + e.getMessage(), e);
        }
    }

    private void writeToFile() {
        try {
            Path folder = file.getParent();
            if (folder != null) {
                Files.createDirectories(folder);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (E entity : findAll()) {
                    writer.write(createEntityAsString(entity));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RepositoryException("Nu pot scrie fisierul " + file + ": " + e.getMessage(), e);
        }
    }

    @Override
    public E save(E entity) {
        E result = super.save(entity);
        if (result == null) {
            writeToFile();
        }
        return result;
    }

    @Override
    public E delete(ID id) {
        E result = super.delete(id);
        if (result != null) {
            writeToFile();
        }
        return result;
    }

    @Override
    public E update(E entity) {
        E result = super.update(entity);
        if (result == null) {
            writeToFile();
        }
        return result;
    }
}
