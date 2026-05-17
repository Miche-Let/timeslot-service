package com.michelet.timeslotservice.presentation.dto.response;

import java.time.LocalDate;


/**
 * 프론트엔드의 월간 달력 UI에 필요한 날짜별 예약 가능 여부 정보를 담는 DTO입니다.
 */
public record TimeSlotCalendarResponse(
        LocalDate date,
        String status
) {
    public static TimeSlotCalendarResponse of(LocalDate date, boolean isOpened) {
        return new TimeSlotCalendarResponse(date, isOpened ? "OPENED" : "CLOSED");
    }
}