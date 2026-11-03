package repository;

import domain.Entity;
import domain.Validator;

import java.util.LinkedHashMap;
import java.util.Map;

public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {
    private final Validator<E> validator;
    private final Map<ID, E> entities = new LinkedHashMap<>();

    public InMemoryRepository(Validator<E> validator) {
        this.validator = validator;
    }

    @Override
    public E findOne(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return entities.get(id);
    }

    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }

    @Override
    public E save(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }
        validator.validate(entity);
        if (entities.containsKey(entity.getId())) {
            return entity;
        }
        entities.put(entity.getId(), entity);
        return null;
    }

    @Override
    public E delete(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return entities.remove(id);
    }

    @Override
    public E update(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }
        validator.validate(entity);
        if (!entities.containsKey(entity.getId())) {
            return entity;
        }
        entities.put(entity.getId(), entity);
        return null;
    }
}
