package com.michelet.timeslotservice.infrastructure.persistence;

import com.michelet.timeslotservice.infrastructure.config.persistence.TimeSlotRepository;
import com.michelet.timeslotservice.infrastructure.config.persistence.entity.TimeSlotEntity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

import static com.michelet.timeslotservice.support.fixture.TimeSlotFixture.FIXTURE_ID;
import static com.michelet.timeslotservice.support.fixture.TimeSlotFixture.createEntity;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TimeSlotRepository의 데이터베이스 영속성(Persistence)을 검증하는 슬라이스 테스트입니다.
 * <p>
 * {@code @DataJpaTest}를 사용하여 인메모리(H2) 데이터베이스 및 JPA 관련 빈(Bean)만 로드하여
 * 테스트 속도를 높이고 외부 요인을 격리합니다.
 * </p>
 */
@DataJpaTest
@Import(TimeSlotRepositoryTest.JpaAuditingTestConfig.class)
class TimeSlotRepositoryTest {

    /**
     * 테스트 환경 전용 JPA Auditing(생성일, 수정일, 생성자 자동 기록) 설정 클래스입니다.
     * <p>
     * {@code @DataJpaTest}는 기본적으로 메인 애플리케이션의 Auditing 설정을 로드하지 않으므로,
     * 테스트 수행 시 데이터 무결성 위반(NOT NULL 제약조건)을 방지하기 위해 
     * Auditing 기능을 수동으로 활성화하고 가짜(Mock) 사용자 ID를 제공합니다.
     * </p>
     */
    @TestConfiguration
    @EnableJpaAuditing 
    static class JpaAuditingTestConfig {
        
        /**
         * JPA가 엔티티를 저장할 때 '생성자(createdBy)'와 '수정자(updatedBy)'로 기록할
         * 가짜 회원 UUID를 반환하는 빈(Bean)입니다.
         */
        @Bean 
        public AuditorAware<UUID> auditorAware() {
            return () -> Optional.of(UUID.fromString("99999999-9999-9999-9999-999999999999"));
        }
    }

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private TestEntityManager entityManager;

    /**
     * 엔티티가 데이터베이스에 정상적으로 저장되고, JPA Auditing을 통해 생성 시간(createdAt)이 
     * 누락 없이 기록되는지 검증합니다.
     */
    @Test
    @DisplayName("TimeSlotEntity can be saved and found by ID")
    void saveAndFindById_Success() {
        
        // Given: 픽스처를 사용하여 검증에 필요한 최소한의 엔티티 객체를 준비합니다.
        TimeSlotEntity entity = createEntity(4, 4);

        // When: 데이터를 DB에 강제로 기록(Flush)하고, 영속성 컨텍스트(1차 캐시)를 초기화합니다.
        // 이를 통해 다음 findById 호출 시 메모리가 아닌 실제 DB에서 쿼리를 통해 데이터를 가져오도록 강제합니다.
        TimeSlotEntity saved = timeSlotRepository.saveAndFlush(entity); 
        entityManager.clear(); 

        TimeSlotEntity found = timeSlotRepository.findById(saved.getId()).orElseThrow();

        // Then: DB에서 다시 꺼내온 데이터의 식별자가 일치하는지, 
        // 그리고 JPA Auditing이 작동하여 생성 시간이 자동으로 찍혔는지 검증합니다.
        assertThat(found.getId()).isEqualTo(FIXTURE_ID);
        assertThat(found.getCreatedAt()).isNotNull(); 
    }
}