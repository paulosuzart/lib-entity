---
title: Validation
sidebar_label: Validation
---

# Validation

Validation is a core feature of LibEntity that ensures your business rules are enforced at every stage of an entity's lifecycle. You can attach validation logic to fields, states, actions, and transitions using the DSL.

## Why Validate?

- Prevent invalid data from entering your system
- Enforce business constraints at the right time
- Provide clear error messages to users and developers

## Types of Validation

- **Field Validation:** Attach rules to individual fields, optionally scoped to specific states.
- **State Validation:** Validate the entire entity when in a particular state.
- **Transition Validation:** Ensure transitions between states are allowed and safe.
- **Action Validation:** Validate commands and business logic during action execution.

## Example: Field Validation

```java
.field("amount", BigDecimal.class, f -> f
    .validateInState(InvoiceState.DRAFT, (state, entity, req, ctx) -> {
        if (entity.getAmount() == null || entity.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            ctx.addError("INVALID_AMOUNT", "Amount must be greater than zero");
        }
    })
)
```

## Example: Transition Validation

```java
.validateTransition(InvoiceState.PENDING, InvoiceState.APPROVED, (from, to, entity, req, ctx) -> {
    if (entity.getAmount() == null) {
        ctx.addError("AMOUNT_REQUIRED", "Amount must be set before approval");
    }
})
```

## Handling Validation Errors

When validation fails, errors are collected in a context object. You can access these errors and present them to users or log them for debugging.

---

Validation keeps your entities robust and your business logic safe. Use it everywhere you want to guarantee correctness!
