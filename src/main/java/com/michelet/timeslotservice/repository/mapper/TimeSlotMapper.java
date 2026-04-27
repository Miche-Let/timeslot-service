package com.michelet.timeslotservice.repository.mapper;

import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.repository.entity.TimeSlotEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimeSlotMapper {

    TimeSlotEntity toEntity(TimeSlot domain);

    TimeSlot toDomain(TimeSlotEntity entity);
}