---
title: Fields
sidebar_label: Fields
---

# Fields

Fields represent the data properties of your entities in LibEntity. Each field has a name, type, and can have validation rules, default values, and metadata. Fields are declared using the fluent DSL, making your entity definitions expressive and type-safe.

## What is a Field?

A **field** is a property of your entity, such as `amount`, `vat`, or `approverId`. Fields can be of any Java type, including primitives, `String`, `BigDecimal`, enums, dates, and even custom objects.

## Example: Declaring Fields

```java
.field("amount", BigDecimal.class, f -> f
    .description("Invoice amount")
    .validateInState(InvoiceState.DRAFT, (state, entity, req, ctx) -> {
        if (entity.getAmount() == null || entity.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            ctx.addError("INVALID_AMOUNT", "Amount must be greater than zero");
        }
    })
)
```

## Field Types

- **Primitive types:** `int`, `boolean`, etc.
- **Common Java types:** `String`, `BigDecimal`, `LocalDate`, etc.
- **Enums:** For controlled value sets
- **Custom types:** Your own classes

## Field Validation

Fields can have validation logic attached, which is run in specific states or transitions. See the [Validation](../validation/) page for more.

## Field Metadata

You can add descriptions, constraints, and even custom metadata to fields, improving documentation and API generation.

---

Continue to [Validation](../validation/) to learn how to enforce business rules on your entity fields.
