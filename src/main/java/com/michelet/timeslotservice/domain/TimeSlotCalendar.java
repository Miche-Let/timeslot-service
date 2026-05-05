package com.michelet.timeslotservice.domain;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * [일급 컬렉션] 여러 개의 타임슬롯을 감싸고, 특정 월의 '달력' 상태를 분석하는 비즈니스 규칙을 담당합니다.
 */
public class TimeSlotCalendar {

    private final List<TimeSlot> slots;

    public TimeSlotCalendar(List<TimeSlot> slots) {
        this.slots = slots;
    }

    /**
     * 전체 슬롯을 분석하여 해당 월의 달력 상태를 순수 Map으로 반환합니다.
     * @return Map<날짜, OPENED/CLOSED 상태>
     */
    public Map<LocalDate, TimeSlotStatus> getCalendar(YearMonth yearMonth) {
        Map<LocalDate, List<TimeSlot>> slotsByDate = this.slots.stream()
                .collect(Collectors.groupingBy(TimeSlot::getTargetDate));

        return IntStream.rangeClosed(1, yearMonth.lengthOfMonth())
                .mapToObj(yearMonth::atDay)
                .collect(Collectors.toMap(
                        date -> date,
                        date -> determineDailyStatus(slotsByDate.getOrDefault(date, List.of())),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    /**
     * 하루 단위로 타임슬롯들을 분석하여, 하나라도 OPENED 상태인 타임슬롯이 있으면 해당 날짜는 OPENED, 그렇지 않으면 CLOSED로 간주합니다.
     * @param dailySlots
     * @return
     */
    private TimeSlotStatus determineDailyStatus(List<TimeSlot> dailySlots) {
        boolean hasOpenedSlot = dailySlots.stream()
                .anyMatch(slot -> slot.getStatus() == TimeSlotStatus.OPENED);
        
        return hasOpenedSlot ? TimeSlotStatus.OPENED : TimeSlotStatus.CLOSED;
    }
}