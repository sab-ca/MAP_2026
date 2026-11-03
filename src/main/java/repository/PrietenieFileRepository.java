package repository;

import domain.Prietenie;
import domain.Tuple;
import domain.Validator;

import java.time.LocalDateTime;

public class PrietenieFileRepository extends AbstractFileRepository<Tuple<Long, Long>, Prietenie> {

    public PrietenieFileRepository(Validator<Prietenie> validator, String fileName) {
        super(validator, fileName);
    }

    @Override
    protected Prietenie extractEntity(String line) {
        String[] fields = line.split(";", -1);
        if (fields.length != 3) {
            throw new IllegalArgumentException(
                    "se asteapta 3 campuri (idUtilizator1;idUtilizator2;dataPrieteniei), nu " + fields.length);
        }
        Long id1 = Long.parseLong(fields[0].trim());
        Long id2 = Long.parseLong(fields[1].trim());
        LocalDateTime data = LocalDateTime.parse(fields[2].trim());
        return new Prietenie(id1, id2, data);
    }

    @Override
    protected String createEntityAsString(Prietenie p) {
        return p.getIdUtilizator1() + ";" + p.getIdUtilizator2() + ";" + p.getDataPrieteniei();
    }
}
