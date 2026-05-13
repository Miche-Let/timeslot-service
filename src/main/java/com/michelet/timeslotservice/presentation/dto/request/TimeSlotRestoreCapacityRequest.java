package com.michelet.timeslotservice.presentation.dto.request;

import jakarta.validation.constraints.Min;

/**
 * 타임슬롯 인원 복구 요청 DTO
 */
public record TimeSlotRestoreCapacityRequest(

    @Min(value = 1, message = "복구 인원은 최소 1명 이상이어야 합니다.")
    int restoreCapacity
) {
}