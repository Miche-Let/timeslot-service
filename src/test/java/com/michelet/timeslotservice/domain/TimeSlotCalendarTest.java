package com.michelet.timeslotservice.domain;

import com.michelet.timeslotservice.support.builder.TimeSlotTestBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TimeSlotCalendar은 월별 타임슬롯 상태를 계산하는 일급 컬렉션입니다.
 * - 각 날짜별로 OPENED, CLOSED 상태를 계산하여 달력 형태로 반환하는 기능을 검증합니다.
 */
class TimeSlotCalendarTest {

    /**
     * 월별 타임슬롯 목록을 기반으로 날짜별 OPEN/CLOSED 상태를 정확히 계산한다.
     */
    @Test
    @DisplayName("월별 타임슬롯 목록을 기반으로 날짜별 OPEN/CLOSED 상태를 정확히 계산한다.")
    void getCalendar_Success() {

        TimeSlot openSlot = TimeSlotTestBuilder.aTimeSlot()
                .targetDate(LocalDate.of(2036, 5, 1))
                .status(TimeSlotStatus.OPENED)
                .build();
                
        TimeSlot closedSlot1 = TimeSlotTestBuilder.aTimeSlot()
                .targetDate(LocalDate.of(2036, 5, 2))
                .status(TimeSlotStatus.CLOSED)
                .build();
                
        TimeSlot closedSlot2 = TimeSlotTestBuilder.aTimeSlot()
                .targetDate(LocalDate.of(2036, 5, 2))
                .status(TimeSlotStatus.CLOSED)
                .build();


        TimeSlotCalendar calendar = new TimeSlotCalendar(List.of(openSlot, closedSlot1, closedSlot2));


        Map<LocalDate, TimeSlotStatus> result = calendar.getCalendar(YearMonth.of(2036, 5));


        assertThat(result).hasSize(31);
        
        assertThat(result.get(LocalDate.of(2036, 5, 1))).isEqualTo(TimeSlotStatus.OPENED);
        
        assertThat(result.get(LocalDate.of(2036, 5, 2))).isEqualTo(TimeSlotStatus.CLOSED);
        
        assertThat(result.get(LocalDate.of(2036, 5, 3))).isEqualTo(TimeSlotStatus.CLOSED);
    }
}