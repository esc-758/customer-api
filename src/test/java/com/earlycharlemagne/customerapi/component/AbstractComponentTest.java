package com.earlycharlemagne.customerapi.component;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@Testcontainers
@Transactional
abstract class AbstractComponentTest {
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Container
    static final PostgreSQLContainer<?> POSTGRES_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:15.3");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        POSTGRES_SQL_CONTAINER.start();

        registry.add("spring.datasource.url", CustomerComponentTests.POSTGRES_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", CustomerComponentTests.POSTGRES_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", CustomerComponentTests.POSTGRES_SQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", CustomerComponentTests.POSTGRES_SQL_CONTAINER::getDriverClassName);
    }
}
