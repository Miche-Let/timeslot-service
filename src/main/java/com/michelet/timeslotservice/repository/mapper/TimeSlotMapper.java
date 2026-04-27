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

    /**
 * Converts a TimeSlot domain object to its JPA entity representation.
 *
 * @param domain the TimeSlot domain object to convert
 * @return the corresponding TimeSlotEntity representing the domain object
 */
    TimeSlotEntity toEntity(TimeSlot domain);

    /**
 * Maps a TimeSlot JPA entity to its domain representation.
 *
 * @param entity the TimeSlot JPA entity to convert
 * @return the corresponding TimeSlot domain object
 */
    TimeSlot toDomain(TimeSlotEntity entity);
}