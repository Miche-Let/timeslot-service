package com.michelet.timeslotservice.application.service;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.TimeSlotStatus;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;
import com.michelet.timeslotservice.infrastructure.persistence.TimeSlotRepository;
import com.michelet.timeslotservice.infrastructure.persistence.entity.TimeSlotEntity;
import com.michelet.timeslotservice.infrastructure.persistence.mapper.TimeSlotMapper;
import com.michelet.timeslotservice.presentation.dto.request.TimeSlotBulkCreateRequest;
import com.michelet.timeslotservice.support.builder.TimeSlotTestBuilder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * TimeSlotService의 비즈니스 로직을 격리된 환경에서 검증하는 단위 테스트(Unit Test)입니다.
 */
@ExtendWith(MockitoExtension.class)
class TimeSlotServiceTest {

    @InjectMocks
    private TimeSlotService timeSlotService;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private TimeSlotMapper timeSlotMapper;

    /**
     * [Unit Test] 타임슬롯 예약 인원 차감 성공 케이스
     */
    @Test
    @DisplayName("특정 식당과 날짜의 예약 가능한 타임슬롯 목록을 정상적으로 조회한다.")
    void getAvailableTimeSlots_Success() {

        TimeSlotEntity entity = TimeSlotTestBuilder.aTimeSlot().buildEntity();
        TimeSlot domain = TimeSlotTestBuilder.aTimeSlot().build();

        given(timeSlotRepository.findAllByRestaurantIdAndTargetDate(TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID, TimeSlotTestBuilder.DEFAULT_DATE))
                .willReturn(List.of(entity));
        given(timeSlotMapper.toDomain(entity)).willReturn(domain);

        List<TimeSlot> result = timeSlotService.getTimeSlotsByDate(TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID, TimeSlotTestBuilder.DEFAULT_DATE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRestaurantId()).isEqualTo(TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID);
    }

    /**
     * [Unit Test] 타임슬롯 예약 인원 차감 실패 케이스 - 타임슬롯이 존재하지 않는 경우
     */
    @Test
    @DisplayName("타임슬롯의 수용 인원을 정상적으로 차감한다.")
    void deductCapacity_Success() {
        TimeSlotEntity entity = TimeSlotTestBuilder.aTimeSlot().buildEntity();
        TimeSlot domain = TimeSlotTestBuilder.aTimeSlot().build();
        TimeSlotEntity updatedEntity = TimeSlotTestBuilder.aTimeSlot().remainingCapacity(2).buildEntity();

        given(timeSlotRepository.findById(TimeSlotTestBuilder.DEFAULT_ID)).willReturn(Optional.of(entity));
        given(timeSlotMapper.toDomain(entity)).willReturn(domain);
        given(timeSlotMapper.toEntity(domain)).willReturn(updatedEntity);

        timeSlotService.deductCapacity(TimeSlotTestBuilder.DEFAULT_ID, 2);

        assertThat(domain.getRemainingCapacity()).isEqualTo(2);
        verify(timeSlotRepository).save(updatedEntity);
    }

    /**
     * [Unit Test] 타임슬롯 예약 인원 차감 실패 케이스 - 잔여 인원이 부족한 경우
     */
    @Test
    @DisplayName("존재하지 않는 타임슬롯을 차감하려고 하면 예외가 발생한다.")
    void deductCapacity_Fail_NotFound() {
        given(timeSlotRepository.findById(TimeSlotTestBuilder.DEFAULT_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> timeSlotService.deductCapacity(TimeSlotTestBuilder.DEFAULT_ID, 2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(TimeSlotErrorCode.TIME_SLOT_NOT_FOUND.getMessage());
    }

    /**
     * [Unit Test] 타임슬롯 예약 인원 차감 실패 케이스 - 잔여 인원이 부족한 경우
     */
    @Test
    @DisplayName("잔여 인원보다 많은 인원을 차감하려고 하면 예외가 발생한다.")
    void deductCapacity_Fail_NotEnoughCapacity() {
        TimeSlotEntity entity = TimeSlotTestBuilder.aTimeSlot().remainingCapacity(1).buildEntity();
        TimeSlot domain = TimeSlotTestBuilder.aTimeSlot().remainingCapacity(1).build();

        given(timeSlotRepository.findById(TimeSlotTestBuilder.DEFAULT_ID)).willReturn(Optional.of(entity));
        given(timeSlotMapper.toDomain(entity)).willReturn(domain);

        assertThatThrownBy(() -> timeSlotService.deductCapacity(TimeSlotTestBuilder.DEFAULT_ID, 2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(TimeSlotErrorCode.NOT_ENOUGH_CAPACITY.getMessage());
    }

    /**
     * [Unit Test] 타임슬롯 일괄 생성 성공 케이스
     */
    @Test
    @DisplayName("정상적인 조건이 주어지면, 날짜와 시간을 계산하여 알맞은 개수의 타임슬롯을 일괄 생성(saveAll)한다.")
    void createTimeSlotsBulk_Success() {
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 2);
        LocalTime openTime = LocalTime.of(10, 0);
        LocalTime closeTime = LocalTime.of(11, 0);

        TimeSlotBulkCreateRequest request = new TimeSlotBulkCreateRequest(
                startDate, endDate, openTime, closeTime, 30, 4
        );

        given(timeSlotMapper.toEntity(any(TimeSlot.class)))
                .willReturn(TimeSlotTestBuilder.aTimeSlot().buildEntity());

        timeSlotService.createTimeSlotsBulk(TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID, request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TimeSlotEntity>> captor = ArgumentCaptor.forClass(List.class);
        
        verify(timeSlotRepository, times(1)).saveAll(captor.capture());
        
        List<TimeSlotEntity> savedEntities = captor.getValue();
        assertThat(savedEntities).hasSize(4);
    }

    /**
     * [Unit Test] 타임슬롯 일괄 생성 실패 케이스 - 생성될 타임슬롯의 종료 시간이 영업 마감 시간을 초과하는 경우
     */
    @Test
    @DisplayName("생성될 타임슬롯의 종료 시간이 영업 마감 시간을 초과하면 해당 슬롯은 버려진다.")
    void createTimeSlotsBulk_SkipExceedingTime() {
        LocalDate targetDate = LocalDate.of(2026, 5, 1);
        LocalTime openTime = LocalTime.of(10, 0);
        LocalTime closeTime = LocalTime.of(10, 45);

        TimeSlotBulkCreateRequest request = new TimeSlotBulkCreateRequest(
                targetDate, targetDate, openTime, closeTime, 30, 4
        );

        given(timeSlotMapper.toEntity(any(TimeSlot.class)))
                .willReturn(TimeSlotTestBuilder.aTimeSlot().buildEntity());

        timeSlotService.createTimeSlotsBulk(TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID, request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TimeSlotEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(timeSlotRepository).saveAll(captor.capture());

        assertThat(captor.getValue()).hasSize(1);
    }

    /**
     * [Unit Test] 타임슬롯 일괄 생성 실패 케이스 - 생성될 타임슬롯이 이미 존재하는 경우
     */
    @Test
    @DisplayName("특정 연/월 달력 조회 시 1일부터 말일까지의 예약 상태가 정확히 집계된다.")
    void getCalendarByMonth_Success() {
        int year = 2099;
        int month = 5;
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = LocalDate.of(year, month, 31);

        TimeSlotEntity openSlot = org.mockito.Mockito.mock(TimeSlotEntity.class);
        given(openSlot.getTargetDate()).willReturn(LocalDate.of(2099, 5, 1));
        given(openSlot.getStatus()).willReturn(TimeSlotStatus.OPENED);

        TimeSlotEntity closedSlot = org.mockito.Mockito.mock(TimeSlotEntity.class);
        given(closedSlot.getTargetDate()).willReturn(LocalDate.of(2099, 5, 2));
        given(closedSlot.getStatus()).willReturn(TimeSlotStatus.CLOSED);

        given(timeSlotRepository.findAllByRestaurantIdAndTargetDateBetween(TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID, startDate, endDate))
                .willReturn(List.of(openSlot, closedSlot));

        var result = timeSlotService.getCalendarByMonth(TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID, year, month);

        assertThat(result).hasSize(31);
        
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2099, 5, 1));
        assertThat(result.get(0).status()).isEqualTo("OPENED");

        assertThat(result.get(1).date()).isEqualTo(LocalDate.of(2099, 5, 2));
        assertThat(result.get(1).status()).isEqualTo("CLOSED");

        assertThat(result.get(2).date()).isEqualTo(LocalDate.of(2099, 5, 3));
        assertThat(result.get(2).status()).isEqualTo("CLOSED");
    }
}