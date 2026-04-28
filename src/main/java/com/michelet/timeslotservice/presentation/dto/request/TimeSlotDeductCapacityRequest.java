package com.michelet.timeslotservice.presentation.dto.request;

/**
 * 타임슬롯 인원 차감 요청 DTO
 */
public record TimeSlotDeductCapacityRequest(
        int requiredCapacity
) {
}