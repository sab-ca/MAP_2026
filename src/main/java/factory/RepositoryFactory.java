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
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

public class RepositoryFactory {
    private final Properties config = new Properties();
    private final RepositoryType type;
    private final Map<RepositoryType, Function<Validator<Utilizator>, Repository<Long, Utilizator>>> utilizatori =
            new EnumMap<>(RepositoryType.class);
    private final Map<RepositoryType, Function<Validator<Prietenie>, Repository<Tuple<Long, Long>, Prietenie>>> prietenii =
            new EnumMap<>(RepositoryType.class);

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

        utilizatori.put(RepositoryType.MEMORY, InMemoryRepository::new);
        utilizatori.put(RepositoryType.FILE,
                v -> new UtilizatorFileRepository(v, getRequired("file.utilizatori")));
        utilizatori.put(RepositoryType.DATABASE,
                v -> new UtilizatorDbRepository(v, getRequired("db.url"), getRequired("db.user"), getRequired("db.password")));

        prietenii.put(RepositoryType.MEMORY, InMemoryRepository::new);
        prietenii.put(RepositoryType.FILE,
                v -> new PrietenieFileRepository(v, getRequired("file.prietenii")));
        prietenii.put(RepositoryType.DATABASE,
                v -> new PrietenieDbRepository(v, getRequired("db.url"), getRequired("db.user"), getRequired("db.password")));
    }

    public RepositoryType getType() {
        return type;
    }

    public Repository<Long, Utilizator> createUtilizatorRepository(Validator<Utilizator> validator) {
        return creeaza(utilizatori, validator);
    }

    public Repository<Tuple<Long, Long>, Prietenie> createPrietenieRepository(Validator<Prietenie> validator) {
        return creeaza(prietenii, validator);
    }

    private <V, R> R creeaza(Map<RepositoryType, Function<V, R>> creatori, V validator) {
        return Optional.ofNullable(creatori.get(type))
                .orElseThrow(() -> new RepositoryException("Tip de persistenta netratat: " + type))
                .apply(validator);
    }

    private String getRequired(String key) {
        return Optional.ofNullable(config.getProperty(key))
                .filter(value -> !value.isBlank())
                .map(String::trim)
                .orElseThrow(() -> new RepositoryException("Lipseste valoarea pentru '" + key + "' in config.properties."));
    }
}
