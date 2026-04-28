package com.michelet.timeslotservice.presentation.controller;

import com.michelet.common.response.ApiResponse;
import com.michelet.timeslotservice.application.service.TimeSlotService;
import com.michelet.timeslotservice.presentation.code.TimeSlotSuccessCode;
import com.michelet.timeslotservice.presentation.dto.request.TimeSlotDeductCapacityRequest;
import com.michelet.timeslotservice.presentation.dto.response.TimeSlotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/timeslots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;
    /**
     * 특정 식당의 특정 날짜에 해당하는 타임슬롯 목록을 조회하는 API 엔드포인트.
     * 클라이언트 화면에서 마감된 슬롯을 비활성화 처리할 수 있도록 CLOSED 상태도 포함하여 반환합니다.
     */
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

    /**
     * 특정 타임슬롯의 예약 가능 인원을 차감하는 API 엔드포인트.
     * 트랜잭션 종료 시 낙관적 락(@Version)을 통해 동시성 문제를 방어합니다.
     * @param timeSlotId
     * @param request
     * @return
     */
    @PostMapping("/{timeSlotId}/deduct")
    public ApiResponse<Void> deductCapacity(
            @PathVariable UUID timeSlotId,
            @RequestBody TimeSlotDeductCapacityRequest request) {

        timeSlotService.deductCapacity(timeSlotId, request.requiredCapacity());
        
        return ApiResponse.ok(TimeSlotSuccessCode.DEDUCT_SUCCESS, null);
    }
}