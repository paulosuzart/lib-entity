---
title: EntityStore Persistence Abstraction
---

# EntityStore: Simple and Flexible Persistence

`EntityStore` is a core interface in the library that provides a unified abstraction for loading and saving entities. It is designed to make repository implementations simple and extensible, and to unify both loading and saving operations under a single interface.


## The Interface

```java
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
```

## Example Usage

Here is how you might implement an in-memory repository for an `Invoice` entity in a Spring application:

```java
package com.libentity.example.repository;

import com.libentity.example.model.Invoice;
import com.libentity.core.persistence.EntityStore;
import org.springframework.stereotype.Repository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Repository
public class InvoiceRepository implements EntityStore<Invoice, String> {
    // Dummy example. You would typically use a database or other persistence layer.
    private final Map<String, Invoice> database = new ConcurrentHashMap<>();

    @Override
    public Invoice loadById(String id) {
        return database.get(id);
    }

    @Override
    public void save(Invoice invoice) {
        database.put(invoice.getEmployeeId(), invoice);
    }
}
```

## When to Use EntityStore

- When you want a clean, generic interface for repository or persistence logic
- When you want to keep your service and controller layers decoupled from storage details
- When you want to easily swap between in-memory, database, or mock implementations

## See Also
- [Repository Pattern](https://martinfowler.com/eaaCatalog/repository.html)
- [Spring Data Repositories](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#repositories)
