package com.michelet.timeslotservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.michelet.timeslotservice.infrastructure.persistence.entity.TimeSlotEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


/**
 * 타임슬롯 데이터의 영속성(DB 저장/조회)을 관리하는 Spring Data JPA 리포지토리.
 */
public interface TimeSlotRepository extends JpaRepository<TimeSlotEntity, UUID> {
 
    /**
     * 특정 식당의 특정 날짜에 해당하는 모든 타임슬롯을 조회합니다.
     */
    List<TimeSlotEntity> findAllByRestaurantIdAndTargetDate(UUID restaurantId, LocalDate targetDate);

}