package com.libentity.core.persistence;

/**
 * Basic abstraction for loading and saving entities.
 *
 * @param <E> Entity type
 * @param <ID> Identifier type
 */
public interface EntityStore<E, ID> {

    /**
     * Loads an entity by its identifier.
     *
     * @param id Identifier of the entity
     * @return The loaded entity or null if not found
     */
    E loadById(ID id);

    /**
     * Saves the given entity.
     *
     * @param entity Entity to save
     */
    void save(E entity);
}
