package com.michelet.timeslotservice.application.service;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;
import com.michelet.timeslotservice.domain.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimeSlotCapacityService {

    private final TimeSlotRepository timeSlotRepository;

    @Transactional
    public void deductCapacityInTransaction(UUID timeSlotId, int deductCapacity) {
        TimeSlot domain = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new BusinessException(TimeSlotErrorCode.TIME_SLOT_NOT_FOUND));
        domain.deduct(deductCapacity);
        timeSlotRepository.saveAndFlush(domain);
    }

    @Transactional
    public void restoreCapacityInTransaction(UUID timeSlotId, int restoreCapacity) {
        TimeSlot domain = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new BusinessException(TimeSlotErrorCode.TIME_SLOT_NOT_FOUND));
        domain.restore(restoreCapacity);
        timeSlotRepository.saveAndFlush(domain);
    }
}