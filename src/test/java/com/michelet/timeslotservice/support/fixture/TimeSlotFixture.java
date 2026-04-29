package com.michelet.timeslotservice.support.fixture;

import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.TimeSlotStatus;
import com.michelet.timeslotservice.infrastructure.config.persistence.entity.TimeSlotEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * 타임슬롯 관련 테스트 데이터를 일관되게 생성하기 위한 Object Mother (Fixture) 클래스.
 * <p>
 * 테스트 코드의 가독성을 높이고, 객체 생성 보일러플레이트를 최소화하기 위해 사용됩니다.
 * 테스트 검증(Assertion)의 예측 가능성을 보장하기 위해 식별자(ID) 및 시간 정보는 정적 상수(Static Final)로 고정하며,
 * 비즈니스 로직 검증에 핵심적인 영향을 미치는 '수용 인원(Capacity)' 관련 변수만 매개변수로 입력받아 동적으로 생성합니다.
 * </p>
 */
public class TimeSlotFixture {

    /** * 고정된 타임슬롯 ID. 
     * 테스트 검증 시 예측 가능성을 확보하고 랜덤 값으로 인한 Flaky Test를 방지하기 위해 상수로 관리합니다.
     */
    public static final UUID FIXTURE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    
    /** 고정된 식당 ID. */
    public static final UUID FIXTURE_RESTAURANT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    
    /** 고정된 타겟 날짜 (테스트 기준일). */
    public static final LocalDate FIXTURE_DATE = LocalDate.of(2026, 5, 2);
    
    /** 고정된 시작 시간. */
    public static final LocalTime FIXTURE_START_TIME = LocalTime.of(12, 0);
    
    /** 고정된 종료 시간. */
    public static final LocalTime FIXTURE_END_TIME = LocalTime.of(13, 0);

    /**
     * 비즈니스 로직(예: 인원 차감) 테스트를 위한 순수 도메인 객체를 생성합니다.
     *
     * @param capacity          타임슬롯의 총 수용 인원
     * @param remainingCapacity 타임슬롯의 현재 잔여 인원
     * @return 픽스처 상수로 기본값이 세팅된 유효한 TimeSlot 도메인 객체
     */
    public static TimeSlot createDomain(int capacity, int remainingCapacity) {
        return TimeSlot.builder()
                .id(FIXTURE_ID)
                .restaurantId(FIXTURE_RESTAURANT_ID)
                .targetDate(FIXTURE_DATE)
                .startTime(FIXTURE_START_TIME)
                .endTime(FIXTURE_END_TIME)
                .capacity(capacity)
                .remainingCapacity(remainingCapacity)
                .status(TimeSlotStatus.OPENED)
                .build();
    }

    public static TimeSlot createDomainWithDateTime(LocalDate date, LocalTime startTime, LocalTime endTime, int capacity, int remainingCapacity) {
        return TimeSlot.builder()
                .id(FIXTURE_ID)
                .restaurantId(FIXTURE_RESTAURANT_ID)
                .targetDate(date)
                .startTime(startTime)
                .endTime(endTime)
                .capacity(capacity)
                .remainingCapacity(remainingCapacity)
                .status(TimeSlotStatus.OPENED)
                .build();
    }

    /**
     * 영속성(JPA Repository) 테스트 및 매퍼 검증을 위한 엔티티 객체를 생성합니다.
     *
     * @param capacity          타임슬롯의 총 수용 인원
     * @param remainingCapacity 타임슬롯의 현재 잔여 인원
     * @return 픽스처 상수로 기본값이 세팅된 TimeSlotEntity 객체
     */
    public static TimeSlotEntity createEntity(int capacity, int remainingCapacity) {
        return TimeSlotEntity.builder()
                .id(FIXTURE_ID)
                .restaurantId(FIXTURE_RESTAURANT_ID)
                .targetDate(FIXTURE_DATE)
                .startTime(FIXTURE_START_TIME)
                .endTime(FIXTURE_END_TIME)
                .capacity(capacity)
                .remainingCapacity(remainingCapacity)
                .status(TimeSlotStatus.OPENED)
                .build();
    }
}