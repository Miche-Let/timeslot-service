package com.michelet.timeslotservice.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TimeSlotGenerator는 지정된 기간과 시간 범위, 간격에 맞춰 타임슬롯을 일괄 생성하는 유틸리티 클래스입니다.
 */
class TimeSlotGeneratorTest {

    /**
     * generateBulk 메서드는 지정된 기간과 시간 범위, 간격에 맞춰 타임슬롯을 일괄 생성한다. 생성된 타임슬롯의 날짜, 시간, 간격이 정확한지 검증한다.
     */
    @Test
    @DisplayName("지정된 기간과 시간 범위, 간격에 맞춰 타임슬롯을 일괄 생성한다.")
    void generateBulk_Success() {

        UUID restaurantId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2036, 5, 1);
        LocalDate endDate = LocalDate.of(2036, 5, 2);
        LocalTime openTime = LocalTime.of(10, 0);
        LocalTime closeTime = LocalTime.of(12, 0);
        int intervalMinutes = 60;
        int capacity = 4;

        List<TimeSlot> slots = TimeSlotGenerator.generateBulk(
                restaurantId, startDate, endDate, openTime, closeTime, intervalMinutes, capacity
        );

        assertThat(slots).hasSize(4);

        assertThat(slots.get(0).getTargetDate()).isEqualTo(startDate);
        assertThat(slots.get(0).getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(slots.get(0).getEndTime()).isEqualTo(LocalTime.of(11, 0));

        assertThat(slots.get(3).getTargetDate()).isEqualTo(endDate);
        assertThat(slots.get(3).getStartTime()).isEqualTo(LocalTime.of(11, 0));
    }

    /**
     * 슬롯 종료 시간이 마감 시간을 초과하면 해당 슬롯은 생성하지 않고 버려진다.
     */
    @Test
    @DisplayName("슬롯 종료 시간이 마감 시간을 초과하면 해당 슬롯은 생성하지 않고 버려진다.")
    void generateBulk_SkipsExceedingTime() {

        LocalDate date = LocalDate.of(2036, 5, 1);
        LocalTime openTime = LocalTime.of(10, 0);
        LocalTime closeTime = LocalTime.of(10, 45);

        List<TimeSlot> slots = TimeSlotGenerator.generateBulk(
                UUID.randomUUID(), date, date, openTime, closeTime, 30, 4
        );

        assertThat(slots).hasSize(1);
        assertThat(slots.get(0).getEndTime()).isEqualTo(LocalTime.of(10, 30));
    }
}