package com.michelet.timeslotservice.presentation.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.michelet.common.response.ApiResponse;
import com.michelet.timeslotservice.application.service.TimeSlotService;
import com.michelet.timeslotservice.presentation.code.TimeSlotSuccessCode;
import com.michelet.timeslotservice.presentation.dto.request.TimeSlotDeductCapacityRequest;
import com.michelet.timeslotservice.presentation.dto.request.TimeSlotRestoreCapacityRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/v1/time-slots")
@RequiredArgsConstructor
public class TimeSlotInternalController {

    private final TimeSlotService timeSlotService;
    
    /**
     * 특정 타임슬롯의 예약 가능 인원을 차감하는 내부 API 엔드포인트.
     * 트랜잭션 및 낙관적 락을 통해 데이터 일관성을 보장합니다.
     * * @param timeSlotId 차감 대상 타임슬롯 식별자
     * @param request 차감 요청 정보 (인원 수)
     * @return 공통 응답 규격
     */    
    @PatchMapping("/{timeSlotId}/deduct")
    public ApiResponse<Void> deductCapacity(
            @PathVariable UUID timeSlotId, 
            @Valid @RequestBody TimeSlotDeductCapacityRequest request) {

        timeSlotService.deductCapacity(timeSlotId, request.deductCapacity());
        return ApiResponse.ok(TimeSlotSuccessCode.DEDUCT_SUCCESS, null);
        
    }

    /**
     * 특정 타임슬롯의 예약 인원을 복원하는 내부 API 엔드포인트.
     * @param timeSlotId
     * @param request
     * @return
     */

    @PatchMapping("/{timeSlotId}/restore")
    public ApiResponse<Void> restoreCapacity(
            @PathVariable UUID timeSlotId,
            @RequestBody @Valid TimeSlotRestoreCapacityRequest request) {
        
        timeSlotService.restoreCapacity(timeSlotId, request.restoreCapacity());
        
        return ApiResponse.ok(TimeSlotSuccessCode.RESTORE_SUCCESS, null);
    }

}
