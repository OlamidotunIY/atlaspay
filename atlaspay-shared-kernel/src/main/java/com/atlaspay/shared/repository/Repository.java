package com.atlaspay.shared.repository;

import java.util.Optional;

/**
 * Generic domain repository interface.
 * To be implemented by infrastructure adapters (e.g., Spring Data JPA).
 */
public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    void deleteById(ID id);
    boolean existsById(ID id);
}
