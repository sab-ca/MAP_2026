package domain;

import exceptions.ValidationException;

public interface Validator<T> {

    void validate(T entity);
}
