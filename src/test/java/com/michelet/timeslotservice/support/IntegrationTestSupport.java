package com.michelet.timeslotservice.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 싱글톤 패턴을 이용한 PostgreSQL Testcontainer 사용
 */
@ActiveProfiles("local")
@SpringBootTest("eureka.client.enabled=false")
@Testcontainers
public abstract class IntegrationTestSupport {

    @SuppressWarnings("resource")
    @ServiceConnection
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("timeslot_test")
            .withUsername("test")
            .withPassword("test");

    static {
        postgresContainer.start();
    }
}