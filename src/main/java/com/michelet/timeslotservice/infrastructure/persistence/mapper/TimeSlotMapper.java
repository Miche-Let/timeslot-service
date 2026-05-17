package com.michelet.timeslotservice.infrastructure.persistence.mapper;

import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.infrastructure.persistence.entity.TimeSlotEntity;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 순수 도메인 객체(TimeSlot)와 JPA 엔티티(TimeSlotEntity) 간의 데이터 변환을 담당하는 매퍼 인터페이스.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimeSlotMapper {

    /**
     * 순수 도메인 객체를 JPA 엔티티로 변환합니다.
     * @param domain 타임슬롯 도메인 객체
     * @return 변환된 엔티티
     */
    TimeSlotEntity toEntity(TimeSlot domain);

    /**
     * JPA 엔티티를 순수 도메인 객체로 변환합니다.
     * @param entity 타임슬롯 JPA 엔티티
     * @return 변환된 도메인 객체
     */
    TimeSlot toDomain(TimeSlotEntity entity);
}