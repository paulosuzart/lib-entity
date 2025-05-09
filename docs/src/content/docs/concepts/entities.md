---
title: Entities
sidebar_label: Entities
---

# Entities

Entities are the core building blocks of LibEntity. An entity represents a business object (such as an Invoice, Payment, or User) with well-defined fields, states, and actions. Entities encapsulate both data and business rules, making your domain logic explicit, type-safe, and easy to maintain.

## What is an Entity?

An **entity** in LibEntity is:
- A Java class or record that holds your business data (fields)
- Associated with a finite set of possible states (e.g., `DRAFT`, `APPROVED`)
- Configured using a fluent, type-safe DSL to declare fields, state transitions, validation, and actions

## Example: Defining an Invoice Entity

```java
public enum InvoiceState {
    DRAFT, PENDING, APPROVED, REJECTED
}

@Data
static class Invoice {
    private BigDecimal amount;
    private String approverId;
}

EntityType<InvoiceState, Object> invoiceType = EntityType.<InvoiceState, Object>builder("Invoice")
    .field("amount", BigDecimal.class, f -> f)
    .action("approve", a -> a
        .allowedStates(Set.of(InvoiceState.PENDING))
        .handler((state, req, cmd, entity, mutator) -> {
            entity.setApproverId(cmd.getApproverId());
            mutator.setState(InvoiceState.APPROVED);
        })
    )
    .build();
```

### About the `mutator` parameter

The `mutator` parameter in an action handler provides a safe, explicit way to mutate the entity's state or fields during command execution. Instead of setting the state directly on the entity, you use `mutator.setState(newState)` to ensure all business rules and validations are respected. This makes state changes predictable and auditable within the LibEntity framework.

## Key Concepts

- **Fields**: Properties/data of the entity (e.g., `amount`, `approverId`).
- **States**: The lifecycle states your entity can be in (e.g., `DRAFT`, `APPROVED`).
- **Actions**: Operations that can be performed on the entity, potentially causing state transitions.
- **Validation**: Rules that enforce business constraints on fields or transitions.

## Why Use Entities?

- Centralize and document your business rules
- Achieve type safety for all entity operations
- Enable advanced features like OpenAPI generation, dynamic filtering, and more

---

Continue to [States](../states/) to learn more about how entity state machines work in LibEntity.
