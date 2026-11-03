package repository;

import domain.Entity;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Repository<ID, E extends Entity<ID>> {

    E findOne(ID id);

    Iterable<E> findAll();

    E save(E entity);

    E delete(ID id);

    E update(E entity);

    default List<E> findAll(Predicate<E> filter) {
        return streamOf(findAll())
                .filter(filter)
                .collect(Collectors.toList());
    }

    static <T> Stream<T> streamOf(Iterable<T> elemente) {
        return StreamSupport.stream(elemente.spliterator(), false);
    }
}
