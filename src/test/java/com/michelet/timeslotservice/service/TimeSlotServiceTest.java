package com.michelet.timeslotservice.service;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.TimeSlotStatus;
import com.michelet.timeslotservice.exception.TimeSlotErrorCode;
import com.michelet.timeslotservice.repository.TimeSlotRepository;
import com.michelet.timeslotservice.repository.entity.TimeSlotEntity;
import com.michelet.timeslotservice.repository.mapper.TimeSlotMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TimeSlotServiceTest {

    @InjectMocks
    private TimeSlotService timeSlotService;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private TimeSlotMapper timeSlotMapper;

    @Test
    @DisplayName("특정 식당과 날짜의 예약 가능한 타임슬롯 목록을 정상적으로 조회한다.")
    void getAvailableTimeSlots_Success() {
        // Given
        UUID timeSlotId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        LocalDate targetDate = LocalDate.of(2026, 5, 2);
        LocalTime startTime = LocalTime.of(12, 0);
        LocalTime endTime = LocalTime.of(13, 0);
        
        // 🌟 모든 필수 파라미터 완벽 주입
        TimeSlotEntity entity = TimeSlotEntity.builder()
                .id(timeSlotId)
                .restaurantId(restaurantId)
                .targetDate(targetDate)
                .startTime(startTime)
                .endTime(endTime)
                .capacity(4)
                .remainingCapacity(4)
                .status(TimeSlotStatus.OPENED)
                .version(0L)
                .build();
                
        TimeSlot domain = TimeSlot.builder()
                .id(timeSlotId)
                .restaurantId(restaurantId)
                .targetDate(targetDate)
                .startTime(startTime)
                .endTime(endTime)
                .capacity(4)
                .remainingCapacity(4)
                .status(TimeSlotStatus.OPENED)
                .version(0L)
                .build();

        given(timeSlotRepository.findAllByRestaurantIdAndTargetDate(restaurantId, targetDate))
                .willReturn(List.of(entity));
        given(timeSlotMapper.toDomain(entity)).willReturn(domain);

        // When
        List<TimeSlot> result = timeSlotService.getAvailableTimeSlots(restaurantId, targetDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRestaurantId()).isEqualTo(restaurantId);
    }

    @Test
    @DisplayName("타임슬롯의 수용 인원을 정상적으로 차감한다.")
    void deductCapacity_Success() {
        // Given
        UUID timeSlotId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        LocalDate targetDate = LocalDate.of(2026, 5, 2);
        LocalTime startTime = LocalTime.of(12, 0);
        LocalTime endTime = LocalTime.of(13, 0);
        
        TimeSlotEntity entity = TimeSlotEntity.builder()
                .id(timeSlotId)
                .restaurantId(restaurantId)
                .targetDate(targetDate)
                .startTime(startTime)
                .endTime(endTime)
                .capacity(4)
                .remainingCapacity(4)
                .status(TimeSlotStatus.OPENED)
                .version(0L)
                .build();
                
        TimeSlot domain = TimeSlot.builder()
                .id(timeSlotId)
                .restaurantId(restaurantId)
                .targetDate(targetDate)
                .startTime(startTime)
                .endTime(endTime)
                .capacity(4)
                .remainingCapacity(4)
                .status(TimeSlotStatus.OPENED)
                .version(0L)
                .build();
                
        TimeSlotEntity updatedEntity = TimeSlotEntity.builder()
                .id(timeSlotId)
                .restaurantId(restaurantId)
                .targetDate(targetDate)
                .startTime(startTime)
                .endTime(endTime)
                .capacity(4)
                .remainingCapacity(2) // 4명에서 2명으로 차감됨
                .status(TimeSlotStatus.OPENED)
                .version(0L)
                .build();

        given(timeSlotRepository.findById(timeSlotId)).willReturn(Optional.of(entity));
        given(timeSlotMapper.toDomain(entity)).willReturn(domain);
        given(timeSlotMapper.toEntity(domain)).willReturn(updatedEntity);

        // When
        timeSlotService.deductCapacity(timeSlotId, 2);

        // Then
        assertThat(domain.getRemainingCapacity()).isEqualTo(2);
        verify(timeSlotRepository).save(updatedEntity); 
    }

    @Test
    @DisplayName("존재하지 않는 타임슬롯을 차감하려고 하면 예외가 발생한다.")
    void deductCapacity_Fail_NotFound() {
        // Given
        UUID timeSlotId = UUID.randomUUID();
        given(timeSlotRepository.findById(timeSlotId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> timeSlotService.deductCapacity(timeSlotId, 2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(TimeSlotErrorCode.TIME_SLOT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("잔여 인원보다 많은 인원을 차감하려고 하면 예외가 발생한다.")
    void deductCapacity_Fail_NotEnoughCapacity() {
        // Given
        UUID timeSlotId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        LocalDate targetDate = LocalDate.of(2026, 5, 2);
        LocalTime startTime = LocalTime.of(12, 0);
        LocalTime endTime = LocalTime.of(13, 0);
        
        TimeSlotEntity entity = TimeSlotEntity.builder()
                .id(timeSlotId)
                .restaurantId(restaurantId)
                .targetDate(targetDate)
                .startTime(startTime)
                .endTime(endTime)
                .capacity(4)
                .remainingCapacity(1) // 1명만 남은 상태
                .status(TimeSlotStatus.OPENED)
                .version(0L)
                .build();
                
        TimeSlot domain = TimeSlot.builder()
                .id(timeSlotId)
                .restaurantId(restaurantId)
                .targetDate(targetDate)
                .startTime(startTime)
                .endTime(endTime)
                .capacity(4)
                .remainingCapacity(1) 
                .status(TimeSlotStatus.OPENED)
                .version(0L)
                .build();

        given(timeSlotRepository.findById(timeSlotId)).willReturn(Optional.of(entity));
        given(timeSlotMapper.toDomain(entity)).willReturn(domain);

        // When & Then (1명 남았는데 2명 차감 요청)
        assertThatThrownBy(() -> timeSlotService.deductCapacity(timeSlotId, 2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(TimeSlotErrorCode.NOT_ENOUGH_CAPACITY.getMessage());
    }
}