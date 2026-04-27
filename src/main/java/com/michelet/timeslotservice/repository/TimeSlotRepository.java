package com.michelet.timeslotservice.repository;

import com.michelet.timeslotservice.repository.entity.TimeSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


/**
 * 타임슬롯 데이터의 영속성(DB 저장/조회)을 관리하는 Spring Data JPA 리포지토리.
 */
public interface TimeSlotRepository extends JpaRepository<TimeSlotEntity, UUID> {
 

}