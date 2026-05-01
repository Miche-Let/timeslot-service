package com.michelet.timeslotservice.application.service;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.TimeSlotStatus;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;
import com.michelet.timeslotservice.infrastructure.persistence.TimeSlotRepository;
import com.michelet.timeslotservice.infrastructure.persistence.entity.TimeSlotEntity;
import com.michelet.timeslotservice.infrastructure.persistence.mapper.TimeSlotMapper;
import com.michelet.timeslotservice.presentation.dto.request.TimeSlotBulkCreateRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 타임슬롯과 관련된 핵심 비즈니스 로직을 처리하는 서비스 계층.
 * 프레젠테이션 계층(Controller)과 영속성 계층(Repository) 사이에서 도메인 규칙을 조율합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotMapper timeSlotMapper;

    /**
     * 특정 식당의 지정된 날짜(TargetDate)에 해당하는 전체 타임슬롯 목록을 조회합니다.
     * 
     * @param restaurantId 식당 식별자
     * @param targetDate 조회하고자 하는 기준 날짜
     * @return 검증된 순수 도메인(TimeSlot) 객체 리스트
     */
    @Transactional(readOnly = true)
    public List<TimeSlot> getTimeSlotsByDate(UUID restaurantId, LocalDate targetDate) { 
        return timeSlotRepository.findAllByRestaurantIdAndTargetDate(restaurantId, targetDate)
                .stream()
                .map(timeSlotMapper::toDomain) // DB 데이터를 순수 도메인으로 번역하여 컨트롤러에 반환
                .collect(Collectors.toList());
    }

    /**
     * 특정 타임슬롯의 예약 가능 인원을 차감합니다.
     * 트랜잭션 종료 시 낙관적 락(@Version)을 통해 동시성(초과 예약) 문제를 방어합니다.
     *
     * @param timeSlotId 타임슬롯 식별자
     * @param requiredCapacity 차감할 인원 수
     */
    @Transactional
    public void deductCapacity(UUID timeSlotId, int requiredCapacity) {

        TimeSlotEntity entity = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new BusinessException(TimeSlotErrorCode.TIME_SLOT_NOT_FOUND));

        TimeSlot domain = timeSlotMapper.toDomain(entity);

        domain.deduct(requiredCapacity);

        TimeSlotEntity updatedEntity = timeSlotMapper.toEntity(domain);
        timeSlotRepository.save(updatedEntity);
        
    }

    /**
        * 특정 식당의 타임슬롯을 일괄 생성합니다. (관리자 전용)
        * 
        * @param restaurantId 식당 식별자
        * @param request      일괄 생성 조건 (날짜, 시간, 인원 등)
     */
    @Transactional
    public void createTimeSlotsBulk(UUID restaurantId, TimeSlotBulkCreateRequest request) {

        List<TimeSlotEntity> existingSlots = timeSlotRepository.findAllByRestaurantIdAndTargetDateBetween(
                restaurantId, request.startDate(), request.endDate()
        );

        Set<String> existingSlotKeys = existingSlots.stream()
                .map(slot -> slot.getTargetDate().toString() + ":" + slot.getStartTime().toString())
                .collect(Collectors.toSet());

        List<TimeSlotEntity> entitiesToSave = new ArrayList<>();
        LocalDate currentDate = request.startDate();

        while (!currentDate.isAfter(request.endDate())) {
            LocalTime currentTime = request.openTime();

            while (!currentTime.isAfter(request.closeTime())) {
                LocalTime slotEndTime = currentTime.plusMinutes(request.intervalMinutes());
                
                if (slotEndTime.isAfter(request.closeTime())) {
                    break;
                }

                String slotKey = currentDate.toString() + ":" + currentTime.toString();
                if (existingSlotKeys.contains(slotKey)) {
                    throw new BusinessException(TimeSlotErrorCode.DUPLICATE_TIME_SLOT);
                }

                TimeSlot newTimeSlot = TimeSlot.builder()
                        .restaurantId(restaurantId)
                        .targetDate(currentDate)
                        .startTime(currentTime)
                        .endTime(slotEndTime)
                        .capacity(request.capacity())
                        .remainingCapacity(request.capacity())
                        .status(TimeSlotStatus.OPENED)
                        .build();

                entitiesToSave.add(timeSlotMapper.toEntity(newTimeSlot));
                currentTime = slotEndTime;
            }
            currentDate = currentDate.plusDays(1);
        }

        if (!entitiesToSave.isEmpty()) {
            try {

                timeSlotRepository.saveAll(entitiesToSave); 

            } catch (org.springframework.dao.DataIntegrityViolationException e) {

                throw new BusinessException(TimeSlotErrorCode.DUPLICATE_TIME_SLOT);
                
            }
        }
    }

}