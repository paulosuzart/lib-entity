---
title: Response Types
sidebar_label: Response Types
---

# Response Types & ActionResultBuilder

When you execute an action in LibEntity, you often want to control exactly what response is returned to your API, UI, or integration layer. The `ActionResultBuilder` pattern gives you the flexibility to construct custom responses for any action execution, decoupling your business logic from your API or transport concerns.

## What is `ActionResultBuilder`?

`ActionResultBuilder` is an interface that lets you define how to build a response object as a result of executing an action. You can use it to:
- Build custom DTOs or API responses
- Aggregate validation errors, state changes, and business data
- Integrate with frameworks like Spring, Quarkus, or others

## Why Use ActionResultBuilder?

- **Flexibility:** You decide what your endpoint returns (entity, status, errors, metadata, etc.)
- **Separation of Concerns:** Keeps business logic and response formatting separate
- **Framework Agnostic:** Works with any web/API framework

## Example Usage (Spring)

Suppose you want to return a custom response after submitting an invoice. Here, the response includes both the mutated `Invoice` and an additional `exchangeRate` field, as defined by the generated `InvoiceWithRateResponse`:

```java
// Response DTO
data class InvoiceWithRateResponse {
    private Invoice invoice;
    private BigDecimal exchangeRate;
}

// Service method using ActionResultBuilder
@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final ActionExecutor<InvoiceState, Invoice, Void> actionExecutor;

    public InvoiceWithRateResponse submitInvoice(Invoice invoice, ActionCommand command) {
        ValidationContext ctx = new ValidationContext();
        InvoiceResultBuilder builder = new InvoiceResultBuilder();
        actionExecutor.execute(
            invoice.getState(),
            invoice,
            null, // no extra request context
            ctx,
            command,
            builder
        );
        return builder.getResponse(); // returns InvoiceWithRateResponse
    }
}
```

In this example, `InvoiceResultBuilder` implements `ActionResultBuilder` and knows how to build the right response for your API, including both the updated invoice and any additional data (like the exchange rate).


## When to Use

- When you want fine-grained control over your API responses
- When integrating with different frameworks or output formats
- When you want to capture more than just the mutated entity (e.g., validation errors, audit info, etc.)

---

The `ActionResultBuilder` pattern gives you full control over your response types—use it to make your service APIs robust, flexible, and cleanly separated from core business logic.
