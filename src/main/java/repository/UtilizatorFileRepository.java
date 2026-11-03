package repository;

import domain.Utilizator;
import domain.Validator;

public class UtilizatorFileRepository extends AbstractFileRepository<Long, Utilizator> {

    public UtilizatorFileRepository(Validator<Utilizator> validator, String fileName) {
        super(validator, fileName);
    }

    @Override
    protected Utilizator extractEntity(String line) {
        String[] fields = line.split(";", -1);
        if (fields.length != 3) {
            throw new IllegalArgumentException("se asteapta 3 campuri (id;prenume;nume), nu " + fields.length);
        }
        Utilizator u = new Utilizator(fields[1].trim(), fields[2].trim());
        u.setId(Long.parseLong(fields[0].trim()));
        return u;
    }

    @Override
    protected String createEntityAsString(Utilizator u) {
        return u.getId() + ";" + u.getPrenume() + ";" + u.getNume();
    }
}
