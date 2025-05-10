# Annotation Support Module

This module provides an annotation-based DSL for defining entity types, actions, and validators for the LibEntity framework. It offers a declarative, beginner-friendly alternative to the builder-style DSL found in the `library` module, enabling rapid and readable configuration using Java annotations.

## Quick Comparison: Annotation DSL vs. Core DSL

| Feature               | Annotation DSL (This Module)                | Core DSL (`library` module)                 |
|----------------------|---------------------------------------------|---------------------------------------------|
| Entity Definition    | `@ActionHandlerFor(entity = ...)`           | `EntityType.builder("...")`                |
| Action Definition    | `@Handle`, `@Action`, `@OnlyIf` on methods  | `.action("name", ...)`                      |
| Validator Definition | `@InStateValidator`, `@TransitionValidator` | `.inStateValidator(...)`, `.transitionValidator(...)` |
| Command Type         | POJO                                        | Must implement `ActionCommand` interface    |
| Allowed States       | `@Action(allowedStates = {"STATE"})`        | `.allowedStates(EnumSet.of(...))`           |
| Extensibility        | Add new annotations or handlers             | Extend builder or entity classes            |

---

## Example: Annotation-Based Entity Definition

```java
@ActionHandlerFor(entity = "Invoice")
public class InvoiceActionHandler {
    @Handle
    @Action(name = "submitInvoice", allowedStates = {"DRAFT"})
    public void handle(InvoiceState state, InvoiceRequest request, SubmitInvoiceCommand command, StateMutator<InvoiceState> mutator) {
        mutator.setState(InvoiceState.PENDING_APPROVAL);
    }

    @OnlyIf
    @Action(name = "submitInvoice")
    public boolean canSubmit(InvoiceState state, InvoiceRequest request, SubmitInvoiceCommand command) {
        return request.getAmount() > 0;
    }
}

@Data
@AllArgsConstructor
public class SubmitInvoiceCommand {
    private final String submitDate;
    private final String submitterId;
}
```

## Example: Core (Builder) DSL

```java
EntityType<InvoiceState, InvoiceRequest> invoiceType = EntityType.builder("Invoice")
    .action("submitInvoice", ActionBuilder
        .forHandler((state, request, command, mutator) -> mutator.setState(InvoiceState.PENDING_APPROVAL))
        .onlyIf((state, request, command) -> request.getAmount() > 0)
        .allowedStates(EnumSet.of(InvoiceState.DRAFT))
        .build("submitInvoice")
    )
    .build();
```

---

## Features
- **Declarative Action & Validator Registration:** Define actions, guards, and validators using simple annotations.
- **Command Flexibility:** Commands can be POJOs, no need to implement interfaces.
- **Type Safety:** The processor checks handler signatures and state names at build time.
- **Integration:** Outputs real `EntityType` objects compatible with the core engine.

## How It Works
- Annotate your handler and validator classes/methods.
- The annotation processor scans for these annotations and builds a registry of entity types and actions.
- At runtime, actions and validators are invoked via reflection and proxies, ensuring compatibility with the core engine.

## When to Use
- Prefer this module if you want a quick, annotation-driven, and beginner-friendly way to define entities.
- Use the builder DSL for maximum flexibility, advanced composition, or meta-programming.

## Limitations
- `allowedStates` in annotations must be strings due to Java annotation restrictions (see docs for rationale).
- For maximum type safety, use the builder DSL.

## Instance Factories for Actions and Validators

By default, the annotation processor creates new handler and validator instances using reflection. For advanced scenarios, such as integration with Spring or custom DI frameworks, you can supply a custom instance factory:

```java
EntityAnnotationProcessor processor = new EntityAnnotationProcessor(clazz -> applicationContext.getBean(clazz));
```

Or for manual/test-scoped instances:
```java
MyHandler handler = new MyHandler();
EntityAnnotationProcessor processor = new EntityAnnotationProcessor(clazz -> {
    if (clazz.equals(MyHandler.class)) return handler;
    return clazz.getDeclaredConstructor().newInstance();
});
```

This allows seamless integration with dependency injection frameworks or test doubles, making your annotated actions and validators highly flexible and testable.

### Using with Spring

To integrate with Spring, you can provide an instance factory that looks up beans from the Spring `ApplicationContext`. This allows your handlers and validators to be regular Spring beans with full dependency injection:

```java
import org.springframework.context.ApplicationContext;

ApplicationContext applicationContext = ...; // inject or obtain context
EntityAnnotationProcessor processor = new EntityAnnotationProcessor(clazz -> applicationContext.getBean(clazz));
```

Register your action handlers and validators as `@Component` or `@Service` beans. The processor will use Spring to instantiate and inject dependencies as needed.

*In the future, a dedicated `annotation-support-spring` module may be provided for even smoother integration.*


---

*This module is designed to lower the barrier to entry for new users and speed up development for common entity patterns. Contributions and feedback welcome!*
