package com.michelet.timeslotservice.application.service;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;
import com.michelet.timeslotservice.infrastructure.persistence.TimeSlotRepository;
import com.michelet.timeslotservice.infrastructure.persistence.entity.TimeSlotEntity;
import com.michelet.timeslotservice.infrastructure.persistence.mapper.TimeSlotMapper;
import com.michelet.timeslotservice.presentation.dto.request.TimeSlotBulkCreateRequest;

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

import static com.michelet.timeslotservice.support.fixture.TimeSlotFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * TimeSlotService의 비즈니스 로직을 격리된 환경에서 검증하는 단위 테스트(Unit Test)입니다.
 * <p>
 * 데이터베이스나 외부 인프라에 의존하지 않도록 Mockito를 사용하여 Repository와 Mapper를 가짜 객체(Mock)로 대체합니다.
 * 이를 통해 오직 서비스 계층의 '흐름 제어'와 도메인 객체의 '상태 변화'만을 초고속으로 검증합니다.
 * </p>
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
     * 특정 식당과 날짜 조건에 맞는 타임슬롯을 조회할 때, 
     * Repository에서 가져온 엔티티 리스트가 도메인 리스트로 정상 변환되어 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("특정 식당과 날짜의 예약 가능한 타임슬롯 목록을 정상적으로 조회한다.")
    void getAvailableTimeSlots_Success() {

        TimeSlotEntity entity = createEntity(4, 4);
        TimeSlot domain = createDomain(4, 4);

        given(timeSlotRepository.findAllByRestaurantIdAndTargetDate(FIXTURE_RESTAURANT_ID, FIXTURE_DATE))
                .willReturn(List.of(entity));
        given(timeSlotMapper.toDomain(entity)).willReturn(domain);

        List<TimeSlot> result = timeSlotService.getTimeSlotsByDate(FIXTURE_RESTAURANT_ID, FIXTURE_DATE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRestaurantId()).isEqualTo(FIXTURE_RESTAURANT_ID);
    }

    /**
     * 충분한 잔여 인원이 있을 때 차감 요청이 들어오면,
     * 도메인 로직이 수행되어 인원이 깎이고, 변경된 상태가 Repository를 통해 저장(save)되는지 검증합니다.
     */
    @Test
    @DisplayName("타임슬롯의 수용 인원을 정상적으로 차감한다.")
    void deductCapacity_Success() {
        TimeSlotEntity entity = createEntity(4, 4);
        TimeSlot domain = createDomain(4, 4);
        TimeSlotEntity updatedEntity = createEntity(4, 2);

        given(timeSlotRepository.findById(FIXTURE_ID)).willReturn(Optional.of(entity));
        given(timeSlotMapper.toDomain(entity)).willReturn(domain);
        given(timeSlotMapper.toEntity(domain)).willReturn(updatedEntity);

        timeSlotService.deductCapacity(FIXTURE_ID, 2);

        assertThat(domain.getRemainingCapacity()).isEqualTo(2);
        verify(timeSlotRepository).save(updatedEntity);
    }

    /**
     * 차감 요청 시 제공된 ID에 해당하는 타임슬롯이 DB에 존재하지 않을 경우,
     * TIME_SLOT_NOT_FOUND 예외가 즉시 발생하여 로직이 차단되는지 검증합니다.
     */
    @Test
    @DisplayName("존재하지 않는 타임슬롯을 차감하려고 하면 예외가 발생한다.")
    void deductCapacity_Fail_NotFound() {

        given(timeSlotRepository.findById(FIXTURE_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> timeSlotService.deductCapacity(FIXTURE_ID, 2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(TimeSlotErrorCode.TIME_SLOT_NOT_FOUND.getMessage());
    }

    /**
     * 잔여 인원보다 많은 인원의 차감을 요청했을 때,
     * 도메인 검증 로직에 의해 NOT_ENOUGH_CAPACITY 예외가 발생하여 오버부킹이 차단되는지 검증합니다.
     */
    @Test
    @DisplayName("잔여 인원보다 많은 인원을 차감하려고 하면 예외가 발생한다.")
    void deductCapacity_Fail_NotEnoughCapacity() {

        TimeSlotEntity entity = createEntity(4, 1);
        TimeSlot domain = createDomain(4, 1);

        given(timeSlotRepository.findById(FIXTURE_ID)).willReturn(Optional.of(entity));
        given(timeSlotMapper.toDomain(entity)).willReturn(domain);


        assertThatThrownBy(() -> timeSlotService.deductCapacity(FIXTURE_ID, 2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(TimeSlotErrorCode.NOT_ENOUGH_CAPACITY.getMessage());
    }

    /**
     * 일괄 생성 로직 검증 (정상 흐름):
     * 이중 루프(날짜, 시간)가 정상 작동하여, 지정된 기간과 간격에 맞는 
     * 정확한 개수의 타임슬롯 엔티티가 Repository로 전달되는지 검증합니다.
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
                .willReturn(createEntity(4, 4));

        timeSlotService.createTimeSlotsBulk(FIXTURE_RESTAURANT_ID, request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TimeSlotEntity>> captor = ArgumentCaptor.forClass(List.class);
        
        verify(timeSlotRepository, times(1)).saveAll(captor.capture());
        
        List<TimeSlotEntity> savedEntities = captor.getValue();
        
        assertThat(savedEntities).hasSize(4);
    }

    /**
     * 일괄 생성 로직 검증
     * 영업 마감 시간을 넘어서는 슬롯은 헬퍼 메서드에서 생성하지 않고 버리는지 검증합니다.
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
                .willReturn(createEntity(4, 4));

        timeSlotService.createTimeSlotsBulk(FIXTURE_RESTAURANT_ID, request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TimeSlotEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(timeSlotRepository).saveAll(captor.capture());

        assertThat(captor.getValue()).hasSize(1);
    }

}