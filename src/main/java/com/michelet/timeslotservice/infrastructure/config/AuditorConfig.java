package com.michelet.timeslotservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import com.michelet.common.auth.webmvc.context.UserContextHolder;

import java.util.Optional;
import java.util.UUID;


/**
 * JPA Auditing 기능을 활성화하여 생성자 및 수정자 UUID를 자동 주입하기 위한 설정 클래스.
 */
@Configuration
public class AuditorConfig {

    /**
     * 현재 스레드 로컬(UserContextHolder)에서 작업자의 UUID를 꺼내어 JPA에 제공합니다.
     * @return UUID를 담은 Optional 객체
     */
    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> {
            var context = UserContextHolder.get();
            
            if (context != null && context.userId() != null) {
                return Optional.of(UUID.fromString(context.userId()));
            }
            
            return Optional.empty();
        };
    }
}