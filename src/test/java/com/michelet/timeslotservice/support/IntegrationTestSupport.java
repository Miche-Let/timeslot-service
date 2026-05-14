package com.michelet.timeslotservice.support;

import static org.mockito.BDDMockito.given;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * 싱글톤 패턴을 이용한 PostgreSQL Testcontainer 사용
 */
@ActiveProfiles("local")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, 
    properties = {"eureka.client.enabled=false"}
)
public abstract class IntegrationTestSupport {



    /**
     * Auditor 설정을 담당
     */
    @MockitoBean
    protected AuditorAware<UUID> auditorAware; 

    @BeforeEach
    void setUpAuditor() {
        given(auditorAware.getCurrentAuditor()).willReturn(Optional.of(UUID.randomUUID()));
    }


    /**
     * internal.auth.secret 을 이용하여 X-Internal-Token 생성
     */
    @Value("${internal.auth.secret}")
    private String secretKey;

    protected String createTestInternalToken() {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .issuer("michelet-admin")
                .audience().add("timeslot-service").and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * PostgreSQL 테스트 컨테이너 생성
     */
    @SuppressWarnings("resource")
    @ServiceConnection
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:18.3")
            .withDatabaseName("timeslot_test")
            .withUsername("test")
            .withPassword("test");

    static {
        postgresContainer.start();
    }
}