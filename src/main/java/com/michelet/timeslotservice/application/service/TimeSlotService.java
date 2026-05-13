package com.michelet.timeslotservice.application.service;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.TimeSlotCalendar;
import com.michelet.timeslotservice.domain.TimeSlotGenerator;
import com.michelet.timeslotservice.domain.TimeSlotStatus;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;
import com.michelet.timeslotservice.domain.repository.TimeSlotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * [도메인 서비스] 도메인 모델이 표현하기 어려운 복잡한 비즈니스 로직을 구현합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;


    /**
     * 특정 식당의 특정 일자(TargetDate) 타임슬롯 목록을 조회합니다.
     * @param restaurantId
     * @param targetDate
     * @return
     */
    @Transactional(readOnly = true)
    public List<TimeSlot> getTimeSlotsByDate(UUID restaurantId, LocalDate targetDate) { 
        return timeSlotRepository.findByDate(restaurantId, targetDate);
    }


    /**
     * 특정 타임슬롯에서 예약이 확정되어, 남은 좌석 수를 줄여야 하는 경우에 호출됩니다.
     * @param timeSlotId
     * @param requiredCapacity
     */
    @Transactional
    public void deductCapacity(UUID timeSlotId, int requiredCapacity) {
        TimeSlot domain = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new BusinessException(TimeSlotErrorCode.TIME_SLOT_NOT_FOUND));

        domain.deduct(requiredCapacity);

        timeSlotRepository.save(domain);
    }

    /**
     * 특정 식당의 타임슬롯을 일괄 생성합니다.
     * @param restaurantId
     * @param startDate
     * @param endDate
     * @param openTime
     * @param closeTime
     * @param intervalMinutes
     * @param capacity
     */
    @Transactional
    public void createTimeSlotsBulk(
            UUID restaurantId, LocalDate startDate, LocalDate endDate, 
            LocalTime openTime, LocalTime closeTime, int intervalMinutes, int capacity) {

        List<TimeSlot> candidates = TimeSlotGenerator.generateBulk(
                restaurantId, startDate, endDate, openTime, closeTime, intervalMinutes, capacity
        );

        validateNoDuplicates(restaurantId, startDate, endDate, candidates);
        
        timeSlotRepository.saveAll(candidates);
    }

    /**
     * 특정 식당의 월간 달력(예약 가능 여부)을 조회합니다.
     * @param restaurantId
     * @param year
     * @param month
     * @return Map<날짜, 타임슬롯 상태>
     */
    @Transactional(readOnly = true)
    public Map<LocalDate, TimeSlotStatus> getCalendarByMonth(UUID restaurantId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        
        List<TimeSlot> rawSlots = timeSlotRepository.findByDateRange(
                restaurantId, yearMonth.atDay(1), yearMonth.atEndOfMonth()
        );

        TimeSlotCalendar timeSlotCalendar = new TimeSlotCalendar(rawSlots);
        return timeSlotCalendar.getCalendar(yearMonth);
    }


    /**
     * 일괄 생성하려는 타임슬롯 후보군과 기존에 존재하는 타임슬롯들을 비교하여, 중복되는 타임슬롯이 하나라도 있으면 예외를 던집니다.
     * @param restaurantId
     * @param startDate
     * @param endDate
     * @param candidates
     */
    private void validateNoDuplicates(UUID restaurantId, LocalDate startDate, LocalDate endDate, List<TimeSlot> candidates) {
        List<TimeSlot> existingSlots = timeSlotRepository.findByDateRange(restaurantId, startDate, endDate);
        
        Set<String> existingSlotKeys = existingSlots.stream()
                .map(slot -> slot.getTargetDate().toString() + ":" + slot.getStartTime().toString())
                .collect(Collectors.toSet());

        for (TimeSlot candidate : candidates) {
            String slotKey = candidate.getTargetDate().toString() + ":" + candidate.getStartTime().toString();
            if (existingSlotKeys.contains(slotKey)) {
                throw new BusinessException(TimeSlotErrorCode.DUPLICATE_TIME_SLOT);
            }
        }
    }

    /**
     * 결제 실패 혹은 오류시 타임슬롯 인원을 복원합니다.
     * @param timeSlotId
     * @param recoverCapacity
     */

    @Transactional
    public void restoreCapacity(UUID timeSlotId, int recoverCapacity) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new BusinessException(TimeSlotErrorCode.TIME_SLOT_NOT_FOUND));

        timeSlot.restore(recoverCapacity);

        timeSlotRepository.save(timeSlot);

    }

}