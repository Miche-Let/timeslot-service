package com.michelet.timeslotservice.presentation.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;

/**
 * 관리자의 타임슬롯 일괄 생성 요청 데이터를 담는 DTO
 */
public record TimeSlotBulkCreateRequest(

        @NotNull(message = "시작일은 필수입니다.")
        @FutureOrPresent(message = "시작일은 과거일 수 없습니다.")
        LocalDate startDate,

        @NotNull(message = "종료일은 필수입니다.")
        LocalDate endDate,

        @NotNull(message = "영업 시작 시간은 필수입니다.")
        LocalTime openTime,

        @NotNull(message = "영업 종료 시간은 필수입니다.")
        LocalTime closeTime,

        @NotNull(message = "슬롯 간격은 필수입니다.")
        @Min(value = 10, message = "슬롯 간격은 최소 10분 이상이어야 합니다.")
        Integer intervalMinutes,

        @NotNull(message = "수용 인원은 필수입니다.")
        @Min(value = 1, message = "수용 인원은 최소 1명 이상이어야 합니다.")
        Integer capacity
) {
    /**
     * 시작일이 종료일보다 늦지 않은지 검증합니다. (Null-safe 방어 추가)
     */
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) return false;
        if (startDate.isAfter(endDate)) return false;

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > 31) {
            throw new BusinessException(TimeSlotErrorCode.DATE_RANGE_TOO_LARGE);
        }
        return true;
    }

    /**
     * 영업 오픈 시간이 종료 시간보다 빠른지 검증합니다. (Null-safe 방어 추가)
     */
    public boolean isValidTimeRange() {
        if (openTime == null || closeTime == null) {
            return false;
        }
        return openTime.isBefore(closeTime);
    }

    
}