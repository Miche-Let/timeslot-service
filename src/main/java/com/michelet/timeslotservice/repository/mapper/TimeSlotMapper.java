package com.michelet.timeslotservice.repository.mapper;

import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.repository.entity.TimeSlotEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 순수 도메인 객체(TimeSlot)와 JPA 엔티티(TimeSlotEntity) 간의 데이터 변환을 담당하는 매퍼 인터페이스.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimeSlotMapper {

    TimeSlotEntity toEntity(TimeSlot domain);

    TimeSlot toDomain(TimeSlotEntity entity);
}