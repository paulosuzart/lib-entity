# jooq-support

**jooq-support** is a utility module for integrating LibEntity filter definitions with [jOOQ](https://www.jooq.org/) in Java projects. It provides a bridge between high-level, type-safe filter objects and the construction of dynamic, composable SQL `Condition` objects for use in jOOQ queries. This makes filtering, searching, and querying your database seamless, robust, and maintainable.

---

## Features
- **Automatic translation of LibEntity filter objects to jOOQ `Condition` objects**
- **Support for a wide range of comparators:**
  - Equality (`EQ`), greater/less than (`GT`, `GTE`, `LT`, `LTE`), `IN`, `LIKE`, and boolean
- **Range filtering** via `RangeFilter<T>`
- **Virtual fields** via custom mappers
- **Annotation processor** for generating filter meta-classes
- **Extensible:** Easily add support for new comparators or custom filter logic

---

## Getting Started

### 1. Add Dependency
Add `jooq-support` as a dependency in your Gradle or Maven build. (Assuming you have access to the published artifact or include it as a local module.)

```
dependencies {
    implementation project(':jooq-support')
}
```

### 🚀 Annotation Processor (Recommended)

The easiest and safest way to use filters with jOOQ is via the provided annotation processor. This generates meta-classes for your filters, reducing boilerplate and ensuring correctness.

#### 1. Annotate Your Filter Class
```java
import com.libentity.jooqsupport.annotation.JooqFilter;
import com.libentity.jooqsupport.annotation.JooqFilterField;

@JooqFilter(tableClass = "UserTable", tableVar = "USER")
public class UserFilter {
    @JooqFilterField(field = "id", comparators = {Comparator.EQ})
    public Long id;
    @JooqFilterField(field = "name", comparators = {Comparator.EQ, Comparator.LIKE})
    public String name;
}
```

#### 2. Use the Generated Meta-Class
The annotation processor will generate `UserFilterJooqMeta` for you. Use it like this:

```java
import static com.example.UserFilterJooqMeta.*;

UserFilter filter = new UserFilter();
filter.name = "Alice";
Condition condition = JooqFilterSupport.buildCondition(filter, DEFINITION, FIELD_MAPPING);
```

- `DEFINITION` and `FIELD_MAPPING` are generated constants mapping your filter fields to supported comparators and jOOQ fields.

#### 3. Compile-Time Safety
If you misuse the annotation (e.g., omit required attributes), compilation will fail with a clear error.

---

### Manual Setup (Advanced/Legacy)

If you wish to define everything manually:

#### 1. Define a Filter Class
Create a POJO filter class:
```java
public class UserFilter {
    public Integer age;
    public String name;
    public List<String> roles;
    public RangeFilter<Integer> ageRange;
}
```

#### 2. Define a FilterDefinition
Map your filter fields to supported comparators:
```java
FilterDefinition<UserFilter> definition = new FilterDefinition<>(
    "UserFilter",
    UserFilter.class,
    Map.of(
        "age", Set.of(FieldFilterType.EQ, FieldFilterType.GT, FieldFilterType.LT),
        "name", Set.of(FieldFilterType.EQ, FieldFilterType.LIKE),
        "roles", Set.of(FieldFilterType.IN),
        "ageRange", Set.of(FieldFilterType.GT, FieldFilterType.LT, FieldFilterType.EQ)
    )
);
```

#### 3. Map Logical Fields to jOOQ Fields
```java
Field<Integer> AGE_FIELD = DSL.field("age", Integer.class);
Field<String> NAME_FIELD = DSL.field("name", String.class);
Field<String> ROLES_FIELD = DSL.field("roles", String.class);

Map<String, Field<?>> fieldMapping = Map.of(
    "age", AGE_FIELD,
    "name", NAME_FIELD,
    "roles", ROLES_FIELD
);
```

#### 4. Build a Condition
```java
UserFilter filter = new UserFilter();
filter.age = 30;
filter.roles = List.of("admin", "user");

Condition condition = JooqFilterSupport.buildCondition(filter, definition, fieldMapping);
// Use this condition in your jOOQ query
```

---

## Advanced Usage

### RangeFilter
Supports GT, GTE, LT, LTE, EQ for numeric/date fields:
```java
RangeFilter<Integer> range = new RangeFilter<>();
range.setGt(18);
range.setLt(65);
filter.ageRange = range;
```

### Virtual Fields
For fields not directly mapped to a DB column, implement a `VirtualConditionMapper` and register it in your filter definition.

---

## Annotation Processor Testing

This project uses [compile-testing](https://github.com/google/compile-testing) to ensure the annotation processor works as expected.

- **Positive test:** Verifies that a valid filter class generates the correct meta-class.
- **Negative test:** Ensures that missing required annotation attributes (e.g., `tableClass`) will fail compilation.

See `JooqFilterAnnotationProcessorTest.java` for details.

---

## Example
See `JooqFilterSupportTest.java` for filter logic tests and `JooqFilterAnnotationProcessorTest.java` for annotation processor tests.

---

## Requirements
- Java 17+
- jOOQ 3.18+
- LibEntity core library

---

## License
MIT
