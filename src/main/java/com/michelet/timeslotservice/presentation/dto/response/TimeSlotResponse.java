package com.michelet.timeslotservice.presentation.dto.response;

import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.TimeSlotStatus;

import java.time.LocalTime;
import java.util.UUID;

public record TimeSlotResponse(
        UUID timeSlotId,
        LocalTime startTime,
        LocalTime endTime,
        int capacity,
        int remainingCapacity,
        TimeSlotStatus status
) {
    public static TimeSlotResponse from(TimeSlot domain) {
        return new TimeSlotResponse(
                domain.getId(),
                domain.getStartTime(),
                domain.getEndTime(),
                domain.getCapacity(),
                domain.getRemainingCapacity(),
                domain.getStatus()
        );
    }
}