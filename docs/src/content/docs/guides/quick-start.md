---
title: Quick Start Guide
description: Get started with LibEntity
---

# Quick Start

LibEntity is a powerful (and fun!) Java library for building type-safe, state-driven business entities with validation and action handling. It provides a clean, expressive DSL for defining entities, their states, fields, and actions. It's like Spring Boot for your business rules, but with more good vibes and less boilerplate!

## Features

- 🔒 Type-safe DSL for entity definition
- 🔄 State machine transitions
- ✅ Built-in validation (with easy error handling)
- 🎯 Action-based command pattern
- 📝 Rich field type support
- 🔍 Dynamic filtering
- 📚 OpenAPI generation

## Installation

Add the following to your `build.gradle`:

```groovy
dependencies {
    implementation 'com.libentity:lib-entity:1.0.0'
}
```

## Example: Payment Entity

Let's build a Payment entity together! 💸

```java
// 1. Define the entity's states
public enum PaymentStatus {
    PENDING_AUTHORIZATION,
    PENDING_CAPTURE,
    CAPTURED
}

// 2. Define actions
public enum PaymentAction {
    SET_AMOUNT,
    AUTHORIZE,
    CAPTURE
}

// 3. Define entity interfaces
public interface PaymentEntity {
    BigDecimal getAmount();
    void setAmount(BigDecimal amount);
}

// 4. Create command classes
@Data
public class SetAmountCommand implements ActionCommand {
    private BigDecimal amount;
    @Override
    public String getActionName() { return "SET_AMOUNT"; }
}

// 5. Define the entity type using the new DSL
EntityType<PaymentStatus, PaymentEntity, Object> paymentType = EntityType.<PaymentStatus, PaymentEntity, Object>builder()
    .name("payment")
    .description("Payment entity")
    .field("amount", Field.<BigDecimal, PaymentStatus, PaymentEntity>builder()
        .name("amount")
        .description("Payment amount")
        .getter(PaymentEntity::getAmount)
        .setter(PaymentEntity::setAmount)
        .inStateValidator(PaymentStatus.PENDING_AUTHORIZATION, (state, entity, req, ctx) -> {
            BigDecimal amount = entity.getAmount();
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                ctx.addError("INVALID_AMOUNT", "Amount must be greater than zero");
            }
        })
        .build())
    .action(ActionDefinition.<PaymentStatus, PaymentEntity, Object, SetAmountCommand>builder()
        .name("SET_AMOUNT")
        .allowedStates(Set.of(PaymentStatus.PENDING_AUTHORIZATION))
        .commandType(SetAmountCommand.class)
        .handler((state, req, cmd, entity, mutator) -> {
            entity.setAmount(cmd.getAmount());
        })
        .build());
```

---

For more advanced usage, see the [README](../../../README.md) or the API reference.
