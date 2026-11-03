package factory;

import domain.Prietenie;
import domain.Tuple;
import domain.Utilizator;
import domain.Validator;
import exceptions.RepositoryException;
import repository.InMemoryRepository;
import repository.PrietenieDbRepository;
import repository.PrietenieFileRepository;
import repository.Repository;
import repository.UtilizatorDbRepository;
import repository.UtilizatorFileRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RepositoryFactory {
    private final Properties config = new Properties();
    private final RepositoryType type;

    public RepositoryFactory(String configResource) {
        try (InputStream in = RepositoryFactory.class.getClassLoader().getResourceAsStream(configResource)) {
            if (in == null) {
                throw new RepositoryException("Fisierul de configurare " + configResource
                        + " nu a fost gasit (trebuie sa fie in src/main/resources).");
            }
            config.load(in);
        } catch (IOException e) {
            throw new RepositoryException("Nu pot citi " + configResource + ": " + e.getMessage(), e);
        }
        String value = getRequired("repository.type");
        try {
            type = RepositoryType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RepositoryException("repository.type are valoarea necunoscuta '" + value
                    + "'; valori permise: MEMORY, FILE, DATABASE.", e);
        }
    }

    public RepositoryType getType() {
        return type;
    }

    public Repository<Long, Utilizator> createUtilizatorRepository(Validator<Utilizator> validator) {
        switch (type) {
            case MEMORY:
                return new InMemoryRepository<Long, Utilizator>(validator);
            case FILE:
                return new UtilizatorFileRepository(validator, getRequired("file.utilizatori"));
            case DATABASE:
                return new UtilizatorDbRepository(validator,
                        getRequired("db.url"), getRequired("db.user"), getRequired("db.password"));
            default:
                throw new RepositoryException("Tip de persistenta netratat: " + type);
        }
    }

    public Repository<Tuple<Long, Long>, Prietenie> createPrietenieRepository(Validator<Prietenie> validator) {
        switch (type) {
            case MEMORY:
                return new InMemoryRepository<Tuple<Long, Long>, Prietenie>(validator);
            case FILE:
                return new PrietenieFileRepository(validator, getRequired("file.prietenii"));
            case DATABASE:
                return new PrietenieDbRepository(validator,
                        getRequired("db.url"), getRequired("db.user"), getRequired("db.password"));
            default:
                throw new RepositoryException("Tip de persistenta netratat: " + type);
        }
    }

    private String getRequired(String key) {
        String value = config.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RepositoryException("Lipseste valoarea pentru '" + key + "' in config.properties.");
        }
        return value.trim();
    }
}
