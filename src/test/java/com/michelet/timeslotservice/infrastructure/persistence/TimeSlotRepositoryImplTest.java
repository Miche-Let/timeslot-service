package com.michelet.timeslotservice.infrastructure.persistence;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;
import com.michelet.timeslotservice.infrastructure.persistence.entity.TimeSlotEntity;
import com.michelet.timeslotservice.infrastructure.persistence.mapper.TimeSlotMapper;
import com.michelet.timeslotservice.support.builder.TimeSlotTestBuilder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


/**
 * [인프라 어댑터 단위 테스트]
 */
@ExtendWith(MockitoExtension.class)
class TimeSlotRepositoryImplTest {

    @InjectMocks
    private TimeSlotRepositoryImpl repositoryImpl;

    @Mock
    private TimeSlotJpaRepository jpaRepository;
    @Mock
    private TimeSlotMapper mapper;


    /**
     * findById 메서드는 JPA 리포지토리에서 엔티티를 조회한 후, 매퍼를 통해 도메인 객체로 변환하여 반환한다.
     */
    @Test
    @DisplayName("아이디로 조회 시 JPA 리포지토리에서 찾은 엔티티를 도메인으로 변환하여 반환한다.")
    void findById_Success() {

        TimeSlot domain = TimeSlotTestBuilder.aTimeSlot().build();
        TimeSlotEntity entity = org.mockito.Mockito.mock(TimeSlotEntity.class);

        given(jpaRepository.findById(domain.getId())).willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        Optional<TimeSlot> result = repositoryImpl.findById(domain.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(domain.getId());
    }


    /**
     * save 메서드는 도메인 객체를 엔티티로 변환하여 JPA 리포지토리에 저장한다. 중복 키 에러 발생 시 비즈니스 예외로 변환하여 던진다.
     */
    @Test
    @DisplayName("일괄 저장 시 JPA 리포지토리에 엔티티 리스트를 넘겨 저장한다.")
    void saveAll_Success() {

        TimeSlot domain = TimeSlotTestBuilder.aTimeSlot().build();
        TimeSlotEntity entity = org.mockito.Mockito.mock(TimeSlotEntity.class);

        given(mapper.toEntity(domain)).willReturn(entity);

        repositoryImpl.saveAll(List.of(domain));

        verify(jpaRepository).saveAll(List.of(entity));
    }


    /**
     * saveAll 메서드에서 JPA 리포지토리가 중복 키 에러를 던질 때, 이를 비즈니스 예외(DUPLICATE_TIME_SLOT)로 변환하여 던지는지 검증한다.
     */
    @Test
    @DisplayName("일괄 저장 중 DB 중복 에러가 발생하면 비즈니스 예외(DUPLICATE_TIME_SLOT)로 번역하여 던진다.")
    void saveAll_ThrowsDuplicateException() {

        TimeSlot domain = TimeSlotTestBuilder.aTimeSlot().build();
        TimeSlotEntity entity = org.mockito.Mockito.mock(TimeSlotEntity.class);

        given(mapper.toEntity(domain)).willReturn(entity);
        
        given(jpaRepository.saveAll(any()))
                .willThrow(new DataIntegrityViolationException("Duplicate key constraint violation"));

        assertThatThrownBy(() -> repositoryImpl.saveAll(List.of(domain)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(TimeSlotErrorCode.DUPLICATE_TIME_SLOT.getMessage());
    }
}