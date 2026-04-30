package com.michelet.timeslotservice.application.service;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;
import com.michelet.timeslotservice.infrastructure.persistence.TimeSlotRepository;
import com.michelet.timeslotservice.infrastructure.persistence.entity.TimeSlotEntity;
import com.michelet.timeslotservice.infrastructure.persistence.mapper.TimeSlotMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 타임슬롯과 관련된 핵심 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotMapper timeSlotMapper;


    /**
     * 특정 식당의 지정된 날짜(TargetDate)에 해당하는 전체 타임슬롯 목록을 조회합니다.
     * (클라이언트 화면에서 마감된 슬롯을 비활성화 처리할 수 있도록 CLOSED 상태도 포함하여 반환)
     */
    @Transactional(readOnly = true)
    public List<TimeSlot> getTimeSlotsByDate(UUID restaurantId, LocalDate targetDate) { 
        return timeSlotRepository.findAllByRestaurantIdAndTargetDate(restaurantId, targetDate)
                .stream()
                .map(timeSlotMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 특정 타임슬롯의 예약 가능 인원을 차감합니다.
     * 트랜잭션 종료 시 낙관적 락(@Version)을 통해 동시성 문제를 방어합니다.
     */
    @Transactional
    public void deductCapacity(UUID timeSlotId, int requiredCapacity) {

        TimeSlotEntity entity = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new BusinessException(TimeSlotErrorCode.TIME_SLOT_NOT_FOUND));

        TimeSlot domain = timeSlotMapper.toDomain(entity);

        domain.deduct(requiredCapacity);

        TimeSlotEntity updatedEntity = timeSlotMapper.toEntity(domain);

        timeSlotRepository.save(updatedEntity);
        
        log.info("타임슬롯 차감 성공: ID={}, 요청인원={}, 남은인원={}", 
                timeSlotId, requiredCapacity, domain.getRemainingCapacity());
    }
}