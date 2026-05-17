package com.michelet.timeslotservice.domain;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;
import com.michelet.timeslotservice.support.builder.TimeSlotTestBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TimeSlot은 예약 가능한 시간대를 나타내는 도메인 객체입니다.
 */
class TimeSlotTest {


    /**
     * 잔여 인원을 정상적으로 차감하면 remainingCapacity가 줄어들고, 상태가 OPENED로 유지되는지 검증한다.
     */
    @Test
    @DisplayName("정상적인 인원을 차감하면 잔여 인원이 줄어든다.")
    void deduct_Success() {

        TimeSlot slot = TimeSlotTestBuilder.aTimeSlot().remainingCapacity(4).build();

        slot.deduct(2);

        assertThat(slot.getRemainingCapacity()).isEqualTo(2);
        assertThat(slot.getStatus()).isEqualTo(TimeSlotStatus.OPENED);
    }

    /**
     * 잔여 인원을 모두 차감하면 remainingCapacity가 0이 되고, 상태가 CLOSED로 변경되는지 검증한다.
     */
    @Test
    @DisplayName("잔여 인원을 모두 차감하면 상태가 CLOSED로 변경된다.")
    void deduct_ToZero_ChangesStatusToClosed() {

        TimeSlot slot = TimeSlotTestBuilder.aTimeSlot().remainingCapacity(2).build();

        slot.deduct(2);

        assertThat(slot.getRemainingCapacity()).isEqualTo(0);
        assertThat(slot.getStatus()).isEqualTo(TimeSlotStatus.CLOSED);
    }


    /**
     * 이미 마감된(CLOSED) 슬롯을 차감하려고 하면 예외가 발생한다.
     */
    @Test
    @DisplayName("이미 마감된(CLOSED) 슬롯을 차감하려고 하면 예외가 발생한다.")
    void deduct_Fail_AlreadyClosed() {

        TimeSlot slot = TimeSlotTestBuilder.aTimeSlot().closed().build();

        assertThatThrownBy(() -> slot.deduct(1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(TimeSlotErrorCode.TIME_SLOT_CLOSED.getMessage());
    }


    /**
     * 잔여 인원보다 많은 인원을 차감하려고 하면 예외가 발생한다.
     */
    @Test
    @DisplayName("잔여 인원보다 많은 인원을 차감하려고 하면 예외가 발생한다.")
    void deduct_Fail_NotEnoughCapacity() {

        TimeSlot slot = TimeSlotTestBuilder.aTimeSlot().remainingCapacity(2).build();

        assertThatThrownBy(() -> slot.deduct(3))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(TimeSlotErrorCode.NOT_ENOUGH_CAPACITY.getMessage());
    }
}