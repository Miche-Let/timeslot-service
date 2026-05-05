package com.michelet.timeslotservice.application.service;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.TimeSlotStatus;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;
import com.michelet.timeslotservice.domain.repository.TimeSlotRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.michelet.timeslotservice.support.builder.TimeSlotTestBuilder.aTimeSlot;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

/**
 * [Service Unit Test]
 * Spring 컨텍스트 없이 Mockito로 Repository를 막아두고
 * TimeSlotService의 오케스트레이션 동작을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class TimeSlotServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @InjectMocks
    private TimeSlotService timeSlotService;


    /**
     * 특정 일자의 타임슬롯 목록을 조회한다.
     */
    @Test
    @DisplayName("[Service] 특정 일자의 타임슬롯 목록을 조회한다.")
    void getTimeSlotsByDate_Success() {
        UUID restaurantId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2099, 12, 25);
        TimeSlot slot = aTimeSlot().restaurantId(restaurantId).targetDate(date).build();
        given(timeSlotRepository.findByDate(restaurantId, date)).willReturn(List.of(slot));

        List<TimeSlot> result = timeSlotService.getTimeSlotsByDate(restaurantId, date);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetDate()).isEqualTo(date);
        then(timeSlotRepository).should().findByDate(restaurantId, date);
    }

    /**
     * 타임슬롯 인원 차감에 성공하면 도메인의 deduct를 호출하고 저장한다.
     */
    @Test
    @DisplayName("[Service] 타임슬롯 인원 차감에 성공하면 도메인의 deduct를 호출하고 저장한다.")
    void deductCapacity_Success() {
        UUID timeSlotId = UUID.randomUUID();
        TimeSlot slot = aTimeSlot().id(timeSlotId).capacity(4).remainingCapacity(4).build();
        given(timeSlotRepository.findById(timeSlotId)).willReturn(Optional.of(slot));

        timeSlotService.deductCapacity(timeSlotId, 2);

        assertThat(slot.getRemainingCapacity()).isEqualTo(2);  // 도메인 위임 확인
        then(timeSlotRepository).should().save(slot);
    }

    /**
     * 타임슬롯이 없으면 TIME_SLOT_NOT_FOUND 예외를 던지고 저장하지 않는다.
     */
    @Test
    @DisplayName("[Service] 타임슬롯이 없으면 TIME_SLOT_NOT_FOUND 예외를 던지고 저장하지 않는다.")
    void deductCapacity_NotFound() {
        UUID timeSlotId = UUID.randomUUID();
        given(timeSlotRepository.findById(timeSlotId)).willReturn(Optional.empty());

        BusinessException ex = catchThrowableOfType(
            BusinessException.class,                                  
            () -> timeSlotService.deductCapacity(timeSlotId, 2));

        assertThat(ex).isNotNull();
        assertThat(ex.getErrorCode()).isEqualTo(TimeSlotErrorCode.TIME_SLOT_NOT_FOUND.getCode());
        assertThat(ex.getHttpStatus()).isEqualTo(TimeSlotErrorCode.TIME_SLOT_NOT_FOUND.getHttpStatus());

        then(timeSlotRepository).should(never()).save(any());
    }

    /**
     * 중복이 없으면 후보 슬롯들을 일괄 저장한다.
     */
    @Test
    @DisplayName("[Service] 중복이 없으면 후보 슬롯들을 일괄 저장한다.")
    void createTimeSlotsBulk_Success() {
        UUID restaurantId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2099, 12, 25);
        LocalDate endDate   = LocalDate.of(2099, 12, 25);
        LocalTime openTime  = LocalTime.of(12, 0);
        LocalTime closeTime = LocalTime.of(13, 0);

        // 기존 슬롯 없음 → 중복 없음
        given(timeSlotRepository.findByDateRange(eq(restaurantId), eq(startDate), eq(endDate)))
                .willReturn(List.of());

        timeSlotService.createTimeSlotsBulk(
                restaurantId, startDate, endDate, openTime, closeTime, 60, 4);

        then(timeSlotRepository).should().saveAll(anyList());
    }

    /**
     * 동일 날짜·시작시간의 기존 슬롯이 있으면 DUPLICATE_TIME_SLOT 예외를 던지고 저장하지 않는다.
     */
    @Test
    @DisplayName("[Service] 동일 날짜·시작시간의 기존 슬롯이 있으면 DUPLICATE_TIME_SLOT 예외를 던지고 저장하지 않는다.")
    void createTimeSlotsBulk_Duplicate() {
        UUID restaurantId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2099, 12, 25);
        LocalTime openTime  = LocalTime.of(12, 0);
        LocalTime closeTime = LocalTime.of(13, 0);

        TimeSlot existing = aTimeSlot()
                .restaurantId(restaurantId)
                .targetDate(date)
                .time(openTime, closeTime)
                .build();
        given(timeSlotRepository.findByDateRange(eq(restaurantId), eq(date), eq(date)))
                .willReturn(List.of(existing));

        BusinessException ex = catchThrowableOfType(
        BusinessException.class,
        () -> timeSlotService.createTimeSlotsBulk(
                restaurantId, date, date, openTime, closeTime, 60, 4));

        assertThat(ex).isNotNull();
        assertThat(ex.getErrorCode()).isEqualTo(TimeSlotErrorCode.DUPLICATE_TIME_SLOT.getCode());
        assertThat(ex.getHttpStatus()).isEqualTo(TimeSlotErrorCode.DUPLICATE_TIME_SLOT.getHttpStatus());

        then(timeSlotRepository).should(never()).saveAll(anyList());
    }

    /**
     * 해당 월의 모든 날짜에 대한 달력 상태(Map)를 반환한다.
     */
    @Test
    @DisplayName("[Service] 해당 월의 모든 날짜에 대한 달력 상태(Map)를 반환한다.")
    void getCalendarByMonth_Success() {
        UUID restaurantId = UUID.randomUUID();
        int year = 2099;
        int month = 12;
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay  = LocalDate.of(year, month, 31);

        TimeSlot slot = aTimeSlot()
                .restaurantId(restaurantId)
                .targetDate(LocalDate.of(year, month, 25))
                .build();
        given(timeSlotRepository.findByDateRange(restaurantId, firstDay, lastDay))
                .willReturn(List.of(slot));

        Map<LocalDate, TimeSlotStatus> calendar =
                timeSlotService.getCalendarByMonth(restaurantId, year, month);

        assertThat(calendar).hasSize(31);
        assertThat(calendar).containsKey(LocalDate.of(year, month, 25));
        then(timeSlotRepository).should().findByDateRange(restaurantId, firstDay, lastDay);
    }
}