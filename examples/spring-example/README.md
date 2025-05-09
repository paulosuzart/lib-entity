# Spring Example for lib-entity

This project demonstrates how to use the [lib-entity](https://github.com/paulosuzart/lib-entity) framework in a real-world Spring Boot application. It provides a complete, working example of how to define business entities, states, actions, and validations using the powerful, type-safe DSL provided by lib-entity.

## What’s in This Example?

- **Invoice Entity**: A realistic business entity representing invoices, with fields, states, and business rules.
- **Business Rules as Code**: All business logic—such as allowed actions, state transitions, validations, and field constraints—are encoded declaratively using lib-entity’s DSL.
- **Spring Integration**: Shows how to wire lib-entity into a Spring Boot application, exposing actions and filters via REST endpoints.
- **Clean Architecture**: Demonstrates how lib-entity helps you separate business logic from infrastructure (repositories, controllers, etc.) for maintainable, testable code.

---

## Key Concepts Demonstrated

### 1. Entity Definition & DSL

The heart of the example is [`InvoiceEntityTypeConfig.java`](src/main/java/com/libentity/example/config/InvoiceEntityTypeConfig.java), where the invoice entity is defined using lib-entity’s fluent DSL.

**Features encoded:**
- **Fields**: Amount, VAT, due date, employee, etc.
- **States**: (e.g. `DRAFT`, `SUBMITTED`, `APPROVED`, `REJECTED`, etc.)
- **Actions**: (e.g. `submit`, `approve`, `reject`, `edit`, etc.), each with their own allowed states, handlers, and validation logic.
- **Validations**: Both field-level (e.g. amount > 0, due date in the future) and action-level (e.g. only managers can approve).
- **Reusable Logic**: Common field definitions and validations are shared using interfaces and utility classes.

### 2. Business Rules Encoded

- **Who can perform which actions, and when?**
- **What transitions are allowed between states?**
- **What validations must pass before an action or transition?**
- **How are errors and validation messages handled?**

All these are defined declaratively, making the business logic easy to read, change, and test.

### 3. REST API Endpoints

- **Action Execution**: `/invoice/action` endpoint allows you to trigger invoice actions (like submit, approve, etc.) via HTTP POST.
- **Filtering**: `/invoice/action/filter` endpoint lets you query invoices using flexible filter criteria (amount, due date, employee, etc.).

### 4. Clean, Testable, Maintainable

- **No boilerplate**: Most of the “plumbing” is handled by lib-entity.
- **Focus on business logic**: The code you write is almost entirely business rules and configuration.
- **Easy to extend**: Add new actions, states, or rules with minimal changes.

---

## Running Locally (with Docker)

You can run this example locally using Docker Compose to spin up a Postgres database, and Spring Boot with a special `local-dev` profile.

### 1. Start Postgres with Docker Compose

```sh
cd examples/spring-example
# Start Postgres in the background
docker compose up -d
```

This will launch a Postgres instance with:
- **Database**: `springexample`
- **User**: `springuser`
- **Password**: `springpass`

### 2. Run the Spring Boot App with the local-dev Profile

```sh
# From the root of the repo or the spring-example folder
./gradlew :examples:spring-example:bootRun --args='--spring.profiles.active=local-dev'
```

The app will connect to the Dockerized Postgres automatically. You can now use the REST API as documented above.

---

## How to Use This Example

1. **Clone the repository** and open the `examples/spring-example` module.
2. **Explore the entity configuration** in [`InvoiceEntityTypeConfig.java`](src/main/java/com/libentity/example/config/InvoiceEntityTypeConfig.java).
3. **Check the controllers and services** to see how actions and filters are exposed via Spring REST.
4. **Try out the API**: Use Postman or curl to POST actions or GET filtered invoices.

---

## Why Use lib-entity?

- **Declarative, type-safe business rules**
- **Less boilerplate, more productivity**
- **Beginner-friendly, but powerful for advanced needs**
- **Promotes clean, maintainable, and testable code**

---

**Jump in and see how lib-entity can supercharge your domain-driven design with Spring!**
