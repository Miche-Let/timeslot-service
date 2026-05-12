package com.michelet.timeslotservice.infrastructure.persistence;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;
import com.michelet.timeslotservice.domain.repository.TimeSlotRepository;
import com.michelet.timeslotservice.infrastructure.persistence.entity.TimeSlotEntity;
import com.michelet.timeslotservice.infrastructure.persistence.mapper.TimeSlotMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * [인프라 어댑터] 도메인 인터페이스를 구현하며, JPA와 Entity 매핑을 전담합니다.
 */
@Repository
@RequiredArgsConstructor
public class TimeSlotRepositoryImpl implements TimeSlotRepository {

    private final TimeSlotJpaRepository jpaRepository;
    private final TimeSlotMapper mapper;

    /**
     * 타임슬롯 ID로 단일 타임슬롯을 조회합니다. 존재하지 않으면 Optional.empty()를 반환합니다.
     */
    @Override
    public Optional<TimeSlot> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    /**
     * 특정 식당의 특정 날짜에 해당하는 모든 타임슬롯을 조회합니다. 결과가 없으면 빈 리스트를 반환합니다.
     */
    @Override
    public List<TimeSlot> findByDate(UUID restaurantId, LocalDate targetDate) {
        return jpaRepository.findAllByRestaurantIdAndTargetDate(restaurantId, targetDate)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    /**
     * 특정 식당의 특정 기간 내 타임슬롯을 모두 조회합니다. 결과가 없으면 빈 리스트를 반환합니다.
     */
    @Override
    public List<TimeSlot> findByDateRange(UUID restaurantId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findAllByRestaurantIdAndTargetDateBetween(restaurantId, startDate, endDate)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    /**
     * 타임슬롯을 저장합니다. 이미 존재하는 타임슬롯과 중복되는 경우 예외를 던집니다.
     */
    @Override
    public TimeSlot save(TimeSlot timeSlot) {
        TimeSlotEntity entity = mapper.toEntity(timeSlot);
        TimeSlotEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    /**
     * 여러 타임슬롯을 일괄 저장합니다. 후보군 중 하나라도 기존 타임슬롯과 중복되는 경우 예외를 던집니다.
     */
    @Override
    public List<TimeSlot> saveAll(List<TimeSlot> timeSlots) {
        List<TimeSlotEntity> entities = timeSlots.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());
                List<TimeSlotEntity> savedEntities;
        try {
            savedEntities = jpaRepository.saveAll(entities);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(TimeSlotErrorCode.DUPLICATE_TIME_SLOT);
        }
        return savedEntities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}