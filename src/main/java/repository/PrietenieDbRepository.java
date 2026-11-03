package repository;

import domain.Prietenie;
import domain.Tuple;
import domain.Validator;

public class PrietenieDbRepository implements Repository<Tuple<Long, Long>, Prietenie> {
    private static final String MESAJ =
            "Persistenta in baza de date nu este implementata inca (laboratorul 5).";

    private final Validator<Prietenie> validator;
    private final String url;
    private final String user;
    private final String password;

    public PrietenieDbRepository(Validator<Prietenie> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Prietenie findOne(Tuple<Long, Long> id) {
        throw new UnsupportedOperationException(MESAJ);
    }

    @Override
    public Iterable<Prietenie> findAll() {
        throw new UnsupportedOperationException(MESAJ);
    }

    @Override
    public Prietenie save(Prietenie entity) {
        throw new UnsupportedOperationException(MESAJ);
    }

    @Override
    public Prietenie delete(Tuple<Long, Long> id) {
        throw new UnsupportedOperationException(MESAJ);
    }

    @Override
    public Prietenie update(Prietenie entity) {
        throw new UnsupportedOperationException(MESAJ);
    }
}
