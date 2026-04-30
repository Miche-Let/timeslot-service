package com.michelet.timeslotservice.presentation.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.michelet.common.response.ApiResponse;
import com.michelet.timeslotservice.application.service.TimeSlotService;
import com.michelet.timeslotservice.presentation.code.TimeSlotSuccessCode;
import com.michelet.timeslotservice.presentation.dto.response.TimeSlotResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/timeslots")
@RequiredArgsConstructor
public class TimeSlotExternalController {

    private final TimeSlotService timeSlotService;
    
    @GetMapping
    public ApiResponse<List<TimeSlotResponse>> getTimeSlots(
            @RequestParam UUID restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {

        List<TimeSlotResponse> responses = timeSlotService.getTimeSlotsByDate(restaurantId, targetDate)
                .stream()
                .map(TimeSlotResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.ok(TimeSlotSuccessCode.INQUIRY_SUCCESS, responses);
    }
}
