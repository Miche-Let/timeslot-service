package com.michelet.timeslotservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;
import java.util.UUID;


/**
 * JPA Auditing 기능을 활성화하여 생성자 및 수정자 UUID를 자동 주입하기 위한 설정 클래스.
 */
@Configuration
public class AuditorConfig {

    /**
     * Provides an AuditorAware<UUID> bean that supplies the current worker's UUID.
     *
     * The bean returns a fixed placeholder UUID ("00000000-0000-0000-0000-000000000000") for MVP and local testing;
     * replace with gateway/Security Context integration when available.
     *
     * @return an Optional containing the placeholder UUID used as the current auditor
     */
    @Bean
    public AuditorAware<UUID> auditorAware() {
        // MVP 및 로컬 테스트 통과를 위한 임시 UUID 제공
        // TODO: 추후 게이트웨이/Security Context 연동 시 수정
        return () -> Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }
}