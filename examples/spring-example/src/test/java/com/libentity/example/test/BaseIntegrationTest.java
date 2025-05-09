package com.libentity.example.test;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

@TestConfiguration
public abstract class BaseIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(BaseIntegrationTest.class);
    private static final String DB_NAME = "testdb";
    private static final String DB_USER = "test";
    private static final String DB_PASS = "test";

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER)
            .withPassword(DB_PASS)
            .withInitScript("db/init/init.sql")
            .waitingFor(Wait.forListeningPort());

    @BeforeAll
    static void setup() {
        postgres.start();

        log.info(
                "Connecting to test Postgres container {}:{}@{}",
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword());
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Bean
    public Flyway flyway(DataSource dataSource) {
        final var flyway = Flyway.configure()
                .locations("classpath:db/migration")
                .dataSource(dataSource)
                .cleanDisabled(false)
                .load();

        flyway.clean();
        flyway.migrate();

        return flyway;
    }
}
