---
title: States
sidebar_label: States
---

# States

States are a fundamental concept in LibEntity. They define the lifecycle and behavior of your entities. Each entity can be in exactly one state at any time, and state transitions are governed by your business logic and actions.

## What is a State?

A **state** is a named value (usually represented as a Java `enum`) that describes the current phase or condition of an entity. For example, an `Invoice` might have states like `DRAFT`, `PENDING`, `APPROVED`, and `REJECTED`.

## Why Use States?

- **Model Real-World Lifecycles:** Clearly express how business objects progress through different phases.
- **Control Behavior:** Restrict which actions are available or which fields are editable based on the current state.
- **Validation:** Apply different validation rules depending on the entity's state.

## Example: Defining States

```java
public enum InvoiceState {
    DRAFT, PENDING, APPROVED, REJECTED
}
```

You associate this enum with your entity using the DSL:

```java
EntityType<InvoiceState, Invoice, Object> invoiceType = EntityType.<InvoiceState, Invoice, Object>builder("Invoice")
    .stateEnum(InvoiceState.class)
    // ... fields and actions ...
    .build();
```

## State Transitions

State transitions define how an entity moves from one state to another. In LibEntity, transitions are typically triggered by actions and can be validated or restricted.

For example:

```java
.action("approve", a -> a
    .allowedStates(Set.of(InvoiceState.PENDING))
    .handler((state, req, cmd, entity, mutator) -> {
        mutator.setState(InvoiceState.APPROVED);
    })
)
```

You can also add transition validators:

```java
.validateTransition(InvoiceState.PENDING, InvoiceState.APPROVED, (from, to, entity, req, ctx) -> {
    if (entity.getAmount() == null || entity.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        ctx.addError("AMOUNT_REQUIRED", "Amount must be set and positive");
    }
})
```

## Best Practices

- Use enums for states to ensure type safety and clarity.
- Keep state transitions explicit and document them in your entity definition.
- Use state-based validation to enforce business rules at the right stage of the lifecycle.

---

Continue to [Actions](../actions/) to learn how to trigger state transitions and encapsulate business logic in LibEntity.
