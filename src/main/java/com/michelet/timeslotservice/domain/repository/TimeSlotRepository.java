package com.michelet.timeslotservice.domain.repository;

import com.michelet.timeslotservice.domain.TimeSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * [도메인 포트] 도메인 계층이 인프라(DB)에 의존하지 않기 위해 선언한 순수 인터페이스.
 */
public interface TimeSlotRepository {

    Optional<TimeSlot> findById(UUID id);

    List<TimeSlot> findByDate(UUID restaurantId, LocalDate targetDate);

    List<TimeSlot> findByDateRange(UUID restaurantId, LocalDate startDate, LocalDate endDate);

    TimeSlot save(TimeSlot timeSlot);

    List<TimeSlot> saveAll(List<TimeSlot> timeSlots);
}