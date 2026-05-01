package com.michelet.timeslotservice.infrastructure.persistence.entity;

import com.michelet.common.entity.BaseEntity;
import com.michelet.timeslotservice.domain.TimeSlotStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DB의 p_time_slot 테이블과 매핑되는 JPA 엔티티 클래스.
 */
@Entity
@Table(name = "p_time_slot", 
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uk_time_slot_restaurant_date_start", 
               columnNames = {"restaurant_id", "target_date", "start_time"}
           )
       }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeSlotEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "time_slot_id")
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "remaining_capacity", nullable = false)
    private int remainingCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TimeSlotStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /**
     * 생성자.
     * @param id 타임슬롯 ID
     * @param restaurantId 식당 ID
     * @param targetDate 타겟 날짜
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @param capacity 수용 인원
     * @param remainingCapacity 잔여 수용 인원
     * @param status 상태
     * @param version 버전
     */
    @Builder
    public TimeSlotEntity(UUID id, UUID restaurantId, LocalDate targetDate,
                          LocalTime startTime, LocalTime endTime, int capacity,
                          int remainingCapacity, TimeSlotStatus status, Long version) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.targetDate = targetDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.remainingCapacity = remainingCapacity;
        this.status = (status != null) ? status : TimeSlotStatus.OPENED;
        this.version = version;
    }
}