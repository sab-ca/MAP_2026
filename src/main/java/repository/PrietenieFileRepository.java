package repository;

import domain.Prietenie;
import domain.Tuple;
import domain.Validator;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrietenieFileRepository extends AbstractFileRepository<Tuple<Long, Long>, Prietenie> {

    public PrietenieFileRepository(Validator<Prietenie> validator, String fileName) {
        super(validator, fileName);
    }

    @Override
    protected Prietenie extractEntity(String line) {
        String[] fields = Arrays.stream(line.split(";", -1))
                .map(String::trim)
                .toArray(String[]::new);
        if (fields.length != 3) {
            throw new IllegalArgumentException(
                    "se asteapta 3 campuri (idUtilizator1;idUtilizator2;dataPrieteniei), nu " + fields.length);
        }
        Long[] ids = Arrays.stream(fields, 0, 2)
                .map(Long::parseLong)
                .toArray(Long[]::new);
        return new Prietenie(ids[0], ids[1], LocalDateTime.parse(fields[2]));
    }

    @Override
    protected String createEntityAsString(Prietenie p) {
        return Stream.of(p.getIdUtilizator1(), p.getIdUtilizator2(), p.getDataPrieteniei())
                .map(String::valueOf)
                .collect(Collectors.joining(";"));
    }
}
