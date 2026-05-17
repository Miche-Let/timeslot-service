package com.michelet.timeslotservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.michelet.timeslotservice.infrastructure.persistence.entity.TimeSlotEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


/**
 * [인프라 어댑터] JPA를 활용하여 TimeSlotEntity에 대한 CRUD 작업을 수행하는 리포지토리 인터페이스입니다.
 */
public interface TimeSlotJpaRepository extends JpaRepository<TimeSlotEntity, UUID> {
 
    /**
     * 특정 식당의 특정 날짜에 해당하는 모든 타임슬롯을 조회합니다.
     */
    List<TimeSlotEntity> findAllByRestaurantIdAndTargetDate(UUID restaurantId, LocalDate targetDate);

    /**
     *  특정 식당의 특정 기간 내 타임슬롯을 모두 조회합니다.
     */ 
    List<TimeSlotEntity> findAllByRestaurantIdAndTargetDateBetween(UUID restaurantId, LocalDate startDate, LocalDate endDate);

}