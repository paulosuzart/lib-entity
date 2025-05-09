---
title: Installation
description: Get started with LibEntity in your Java project
---

## Prerequisites

LibEntity requires:

- Java 21 or higher
- Gradle or Maven build system

## Gradle Installation

Add the following to your `build.gradle`:

```groovy
dependencies {
    implementation 'com.libentity:lib-entity:1.0.0'
}
```

## Maven Installation

Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>com.libentity</groupId>
    <artifactId>lib-entity</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Spring Boot Integration

If you're using Spring Boot, we recommend adding these additional dependencies:

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-jooq'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
    implementation 'org.flywaydb:flyway-core'
    runtimeOnly 'org.postgresql:postgresql'
    testImplementation 'org.testcontainers:postgresql'
    testImplementation 'org.testcontainers:junit-jupiter'
}
```

## Verifying Installation

Create a simple test to verify the installation:

```java
import com.libentity.core.EntityType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InstallationTest {
    @Test
    void testLibEntityInstallation() {
        var entityType = EntityType.<TestStatus>builder()
                .name("test")
                .stateEnum(TestStatus.class)
                .build();
        assertNotNull(entityType);
    }
    enum TestStatus {
        ACTIVE, INACTIVE
    }
}

---

For more details, see the [README](../../../README.md).
```

## Next Steps

- [Quick Start Guide](/guides/quick-start/) - Learn how to create your first entity
- [Core Concepts](/concepts/entities/) - Understand the fundamental concepts
- [Spring Boot Example](/advanced/spring-boot/) - See a complete Spring Boot integration
