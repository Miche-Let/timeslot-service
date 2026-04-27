package com.michelet.timeslotservice.repository;

import com.michelet.timeslotservice.repository.entity.TimeSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TimeSlotRepository extends JpaRepository<TimeSlotEntity, UUID> {
 
    
}