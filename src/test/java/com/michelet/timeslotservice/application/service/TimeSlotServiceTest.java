package com.michelet.timeslotservice.application.service;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;
import com.michelet.timeslotservice.infrastructure.persistence.TimeSlotRepository;
import com.michelet.timeslotservice.infrastructure.persistence.entity.TimeSlotEntity;
import com.michelet.timeslotservice.infrastructure.persistence.mapper.TimeSlotMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.michelet.timeslotservice.support.fixture.TimeSlotFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
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
        // Given: 가짜 Repository가 반환할 대본(Stubbing)을 설정합니다.
        TimeSlotEntity entity = createEntity(4, 4);
        TimeSlot domain = createDomain(4, 4);

        given(timeSlotRepository.findAllByRestaurantIdAndTargetDate(FIXTURE_RESTAURANT_ID, FIXTURE_DATE))
                .willReturn(List.of(entity));
        given(timeSlotMapper.toDomain(entity)).willReturn(domain);

        // When: 실제 서비스의 조회 로직을 실행합니다.
        List<TimeSlot> result = timeSlotService.getTimeSlotsByDate(FIXTURE_RESTAURANT_ID, FIXTURE_DATE);

        // Then: 반환된 결과의 크기와 데이터가 정확한지 단언(Assert)합니다.
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
        // Given: 차감 전 상태(4명)와 차감 후 상태(2명)의 데이터를 준비하고 대본을 짭니다.
        TimeSlotEntity entity = createEntity(4, 4);
        TimeSlot domain = createDomain(4, 4);
        TimeSlotEntity updatedEntity = createEntity(4, 2);

        given(timeSlotRepository.findById(FIXTURE_ID)).willReturn(Optional.of(entity));
        given(timeSlotMapper.toDomain(entity)).willReturn(domain);
        given(timeSlotMapper.toEntity(domain)).willReturn(updatedEntity);

        // When: 2명 차감을 요청합니다.
        timeSlotService.deductCapacity(FIXTURE_ID, 2);

        // Then: 도메인 객체의 인원이 2명으로 줄었는지 확인하고, 
        // Repository의 save 메서드가 변경된 엔티티를 파라미터로 받아 호출되었는지 행위를 검증(verify)합니다.
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
        // Given: Repository 조회 시 빈 결과(Optional.empty)가 반환되도록 대본을 짭니다.
        given(timeSlotRepository.findById(FIXTURE_ID)).willReturn(Optional.empty());

        // When & Then: 서비스 로직 실행 중 특정 예외(BusinessException)와 에러 메시지가 발생하는지 검증합니다.
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
        // Given: 잔여 인원이 1명뿐인 타임슬롯 데이터를 준비합니다.
        TimeSlotEntity entity = createEntity(4, 1);
        TimeSlot domain = createDomain(4, 1);

        given(timeSlotRepository.findById(FIXTURE_ID)).willReturn(Optional.of(entity));
        given(timeSlotMapper.toDomain(entity)).willReturn(domain);

        // When & Then: 1명 남았는데 2명을 차감하려 할 때 오버부킹 방지 예외가 터지는지 검증합니다.
        assertThatThrownBy(() -> timeSlotService.deductCapacity(FIXTURE_ID, 2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(TimeSlotErrorCode.NOT_ENOUGH_CAPACITY.getMessage());
    }
}