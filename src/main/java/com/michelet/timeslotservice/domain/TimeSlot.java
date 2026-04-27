package com.michelet.timeslotservice.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.exception.TimeSlotErrorCode;

/**
 * 타임슬롯 도메인 객체.
 * 식당의 예약 가능한 특정 시간대와 수용 인원 상태를 관리합니다.
 */
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

    /**
     * Constructs a TimeSlot with the provided identity, schedule, capacity and audit metadata.
     *
     * The constructor validates that when both `startTime` and `endTime` are provided, `startTime`
     * is strictly before `endTime`. It also validates capacity invariants and defaults `status`
     * to `TimeSlotStatus.OPENED` when `status` is null.
     *
     * @param id               the time slot UUID
     * @param restaurantId     the associated restaurant UUID
     * @param targetDate       the date this time slot applies to
     * @param startTime        the start time of the slot (may be null)
     * @param endTime          the end time of the slot (may be null)
     * @param capacity         total capacity; must be greater than 0
     * @param remainingCapacity remaining capacity; must be >= 0 and <= `capacity`
     * @param status           lifecycle status; if null, defaults to `TimeSlotStatus.OPENED`
     * @param version          optimistic-locking/version value
     * @param createdAt        creation timestamp (may be null)
     * @param createdBy        creator user id (may be null)
     * @param updatedAt        last update timestamp (may be null)
     * @param updatedBy        last updater user id (may be null)
     * @param deletedAt        deletion timestamp (may be null)
     * @param deletedBy        deleter user id (may be null)
     *
     * @throws BusinessException if `startTime` and `endTime` are both non-null and `startTime`
     *                           is not strictly before `endTime` (TimeSlotErrorCode.INVALID_TIME_RANGE)
     * @throws BusinessException if `capacity <= 0` or `remainingCapacity < 0` or
     *                           `remainingCapacity > capacity` (TimeSlotErrorCode.INVALID_CAPACITY)
     */
    @Builder
    public TimeSlot(UUID id, UUID restaurantId, LocalDate targetDate, 
                    LocalTime startTime, LocalTime endTime, int capacity, 
                    int remainingCapacity, TimeSlotStatus status, long version,
                    LocalDateTime createdAt, UUID createdBy, LocalDateTime updatedAt, 
                    UUID updatedBy, LocalDateTime deletedAt, UUID deletedBy) {
        

        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            throw new BusinessException(TimeSlotErrorCode.INVALID_TIME_RANGE);
        }

        if (capacity <= 0 || remainingCapacity < 0 || remainingCapacity > capacity) {
            throw new BusinessException(TimeSlotErrorCode.INVALID_CAPACITY);
        }

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

    /**
     * Deducts the specified number of seats from the time slot's remaining capacity and marks the slot closed when capacity reaches zero.
     *
     * @param requiredCapacity number of seats to deduct
     * @throws BusinessException if {@code requiredCapacity} is less than or equal to zero (TimeSlotErrorCode.INVALID_CAPACITY_REQUEST)
     * @throws BusinessException if the time slot is already closed (TimeSlotErrorCode.TIME_SLOT_CLOSED)
     * @throws BusinessException if remaining capacity is less than {@code requiredCapacity} (TimeSlotErrorCode.NOT_ENOUGH_CAPACITY)
     */
    public void deduct(int requiredCapacity) {
        if (requiredCapacity <= 0) {
        throw new BusinessException(TimeSlotErrorCode.INVALID_CAPACITY_REQUEST);
        }
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