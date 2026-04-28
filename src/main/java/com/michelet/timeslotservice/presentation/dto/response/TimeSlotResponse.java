package com.michelet.timeslotservice.presentation.dto.response;

import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.TimeSlotStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * 타임슬롯 기본 응답 DTO
 */
public record TimeSlotResponse(
        UUID id,
        UUID restaurantId,
        LocalDate targetDate,
        LocalTime startTime,
        LocalTime endTime,
        int capacity,
        int remainingCapacity,
        TimeSlotStatus status
) {
    public static TimeSlotResponse from(TimeSlot domain) {
        return new TimeSlotResponse(
                domain.getId(),
                domain.getRestaurantId(),
                domain.getTargetDate(),
                domain.getStartTime(),
                domain.getEndTime(),
                domain.getCapacity(),
                domain.getRemainingCapacity(),
                domain.getStatus()
        );
    }
}