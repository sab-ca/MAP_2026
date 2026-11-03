package repository;

import domain.Utilizator;
import domain.Validator;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UtilizatorFileRepository extends AbstractFileRepository<Long, Utilizator> {

    public UtilizatorFileRepository(Validator<Utilizator> validator, String fileName) {
        super(validator, fileName);
    }

    @Override
    protected Utilizator extractEntity(String line) {
        String[] fields = Arrays.stream(line.split(";", -1))
                .map(String::trim)
                .toArray(String[]::new);
        if (fields.length != 3) {
            throw new IllegalArgumentException("se asteapta 3 campuri (id;prenume;nume), nu " + fields.length);
        }
        Utilizator u = new Utilizator(fields[1], fields[2]);
        u.setId(Long.parseLong(fields[0]));
        return u;
    }

    @Override
    protected String createEntityAsString(Utilizator u) {
        return Stream.of(String.valueOf(u.getId()), u.getPrenume(), u.getNume())
                .collect(Collectors.joining(";"));
    }
}
