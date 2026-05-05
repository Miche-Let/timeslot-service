package com.michelet.timeslotservice.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * [도메인 서비스] 타임슬롯 일괄 생성의 복잡한 비즈니스 규칙을 캡슐화합니다.
 */
public class TimeSlotGenerator {

    /**
     * 지정된 날짜/시간 범위와 간격에 맞춰 타임슬롯 목록을 생성합니다.
     */
    public static List<TimeSlot> generateBulk(
            UUID restaurantId, LocalDate startDate, LocalDate endDate,
            LocalTime openTime, LocalTime closeTime, int intervalMinutes, int capacity) {

        List<TimeSlot> generatedSlots = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            LocalTime currentTime = openTime;

            while (!currentTime.isAfter(closeTime)) {
                LocalTime slotEndTime = currentTime.plusMinutes(intervalMinutes);
                
                if (slotEndTime.isAfter(closeTime)) {
                    break;
                }

                TimeSlot newTimeSlot = TimeSlot.builder()
                        .restaurantId(restaurantId)
                        .targetDate(currentDate)
                        .startTime(currentTime)
                        .endTime(slotEndTime)
                        .capacity(capacity)
                        .remainingCapacity(capacity)
                        .status(TimeSlotStatus.OPENED)
                        .build();

                generatedSlots.add(newTimeSlot);
                currentTime = slotEndTime;
            }
            currentDate = currentDate.plusDays(1);
        }

        return generatedSlots;
    }
}