package com.michelet.timeslotservice.domain.exception;

import com.michelet.common.exception.BusinessException;

public class ConcurrentModificationException extends BusinessException {
    public ConcurrentModificationException() {
        super(TimeSlotErrorCode.CONCURRENT_MODIFICATION);
    }
}