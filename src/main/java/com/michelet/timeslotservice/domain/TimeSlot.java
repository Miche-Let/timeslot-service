package com.michelet.timeslotservice.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;

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
    private final Long version;

    private final LocalDateTime createdAt;
    private final UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;
    private final LocalDateTime deletedAt;
    private final UUID deletedBy;
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
     * @param createdAt 생성 시간
     * @param createdBy 생성자
     * @param updatedAt 수정 시간
     * @param updatedBy 수정자
     * @param deletedAt 삭제 시간
     * @param deletedBy 삭제자
     */
    @Builder
    public TimeSlot(UUID id, UUID restaurantId, LocalDate targetDate, 
                    LocalTime startTime, LocalTime endTime, int capacity, 
                    int remainingCapacity, TimeSlotStatus status, Long version,
                    LocalDateTime createdAt, UUID createdBy, LocalDateTime updatedAt, 
                    UUID updatedBy, LocalDateTime deletedAt, UUID deletedBy) {
        
        
        if (restaurantId == null || targetDate == null || startTime == null || endTime == null) {
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
        this.status = (status != null)
                ? status
                : (remainingCapacity == 0 ? TimeSlotStatus.CLOSED : TimeSlotStatus.OPENED);
        this.version = version;
        
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    /**
     * 타임슬롯의 예약 가능 인원을 차감합니다.
     * @param requiredCapacity 차감할 인원 수
     * @throws BusinessException 잔여 인원이 부족하거나 마감된 경우 발생
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