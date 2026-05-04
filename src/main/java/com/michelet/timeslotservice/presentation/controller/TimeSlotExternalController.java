package com.michelet.timeslotservice.presentation.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.michelet.common.auth.core.annotation.RequireRole;
import com.michelet.common.auth.core.enums.UserRole;
import com.michelet.common.exception.BusinessException;
import com.michelet.common.response.ApiResponse;
import com.michelet.timeslotservice.application.service.TimeSlotService;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;
import com.michelet.timeslotservice.presentation.code.TimeSlotSuccessCode;
import com.michelet.timeslotservice.presentation.dto.request.TimeSlotBulkCreateRequest;
import com.michelet.timeslotservice.presentation.dto.response.TimeSlotCalendarResponse;
import com.michelet.timeslotservice.presentation.dto.response.TimeSlotResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

/**
 * 프론트엔드(웹/앱) 클라이언트와 통신하는 외부 공개용 API 컨트롤러.
 */
@RestController
@RequestMapping("/api/v1") 
@RequiredArgsConstructor
@Validated
public class TimeSlotExternalController {

    private final TimeSlotService timeSlotService;
    
    /**
     * 특정 식당의 특정 일자(TargetDate) 타임슬롯 목록을 조회합니다.
     */
    @GetMapping("/restaurants/{restaurantId}/time-slots")
    public ApiResponse<List<TimeSlotResponse>> getTimeSlots(
            @PathVariable UUID restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
                
        List<TimeSlotResponse> responses = timeSlotService.getTimeSlotsByDate(restaurantId, targetDate)
                .stream()
                .map(TimeSlotResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.ok(TimeSlotSuccessCode.INQUIRY_SUCCESS, responses);
    }

    /**
     * 특정 식당의 타임슬롯을 일괄 생성합니다. (관리자 전용)
     * 
     * @param restaurantId 식당 식별자
     * @param request      일괄 생성 조건 (날짜, 시간, 인원 등)
     */

    
    @PostMapping("/restaurants/{restaurantId}/time-slots/bulk")
    @RequireRole({UserRole.OWNER, UserRole.MASTER})
    public ApiResponse<Void> createTimeSlotsBulk(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody TimeSlotBulkCreateRequest request) {    

        if (!request.isValidDateRange()) {
            throw new BusinessException(TimeSlotErrorCode.INVALID_DATE_RANGE);
        }
        if (!request.isValidTimeRange()) {
            throw new BusinessException(TimeSlotErrorCode.INVALID_TIME_RANGE);
        }
        
        timeSlotService.createTimeSlotsBulk(restaurantId, request);
        
        return ApiResponse.ok(TimeSlotSuccessCode.BULK_CREATE_SUCCESS, null);
    }
    
    /**
     * 특정 식당의 월간 달력(예약 가능 여부)을 조회합니다.
     */
    @GetMapping("/restaurants/{restaurantId}/time-slots/calendar")
    public ApiResponse<List<TimeSlotCalendarResponse>> getCalendarByMonth(
            @PathVariable UUID restaurantId,
            @RequestParam @Min(2024) int year,
            @RequestParam @Min(1) @Max(12) int month) {

        List<TimeSlotCalendarResponse> responses = timeSlotService.getCalendarByMonth(restaurantId, year, month);

        return ApiResponse.ok(TimeSlotSuccessCode.INQUIRY_SUCCESS, responses);
    }
}