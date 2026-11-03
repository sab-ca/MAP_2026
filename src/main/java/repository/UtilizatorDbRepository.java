package repository;

import domain.Utilizator;
import domain.Validator;

public class UtilizatorDbRepository implements Repository<Long, Utilizator> {
    private static final String MESAJ =
            "Persistenta in baza de date nu este implementata inca (laboratorul 4).";

    private final Validator<Utilizator> validator;
    private final String url;
    private final String user;
    private final String password;

    public UtilizatorDbRepository(Validator<Utilizator> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Utilizator findOne(Long id) {
        throw new UnsupportedOperationException(MESAJ);
    }

    @Override
    public Iterable<Utilizator> findAll() {
        throw new UnsupportedOperationException(MESAJ);
    }

    @Override
    public Utilizator save(Utilizator entity) {
        throw new UnsupportedOperationException(MESAJ);
    }

    @Override
    public Utilizator delete(Long id) {
        throw new UnsupportedOperationException(MESAJ);
    }

    @Override
    public Utilizator update(Utilizator entity) {
        throw new UnsupportedOperationException(MESAJ);
    }
}
