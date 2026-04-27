package com.michelet.timeslotservice.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.exception.TimeSlotErrorCode;

@Getter
public class TimeSlot {
    private final UUID id;
    private final UUID restaurantId;
    private final LocalDate targetDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int capacity;
    private int remainingCapacity;
    private TimeSlotStatus status;
    private final long version;

    private final LocalDateTime createdAt;
    private final UUID createdBy;
    private final LocalDateTime updatedAt;
    private final UUID updatedBy;
    private final LocalDateTime deletedAt;
    private final UUID deletedBy;

    @Builder
    public TimeSlot(UUID id, UUID restaurantId, LocalDate targetDate, 
                    LocalTime startTime, LocalTime endTime, int capacity, 
                    int remainingCapacity, TimeSlotStatus status, long version,
                    LocalDateTime createdAt, UUID createdBy, LocalDateTime updatedAt, 
                    UUID updatedBy, LocalDateTime deletedAt, UUID deletedBy) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.targetDate = targetDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.remainingCapacity = remainingCapacity;
        this.status = status != null ? status : TimeSlotStatus.OPENED;
        this.version = version;
        
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public void deduct(int requiredCapacity) {
        if (this.status == TimeSlotStatus.CLOSED) {
            throw new BusinessException(TimeSlotErrorCode.TIME_SLOT_CLOSED);
        }
        if (this.remainingCapacity < requiredCapacity) {
            throw new BusinessException(TimeSlotErrorCode.NOT_ENOUGH_CAPACITY);
        }

        this.remainingCapacity -= requiredCapacity;

        if (this.remainingCapacity == 0) {
            this.status = TimeSlotStatus.CLOSED;
        }
    }
}