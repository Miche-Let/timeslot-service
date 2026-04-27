package com.michelet.timeslotservice.repository.entity;

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

@Entity
@Table(name = "p_time_slot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeSlotEntity extends BaseEntity {

    @Id
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
        this.status = status;
        this.version = version;
    }
}