package com.michelet.timeslotservice.exception;

import com.michelet.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeSlotErrorCode implements ErrorCode {
    
    TIME_SLOT_NOT_FOUND("TS_001", 404, "해당 타임슬롯을 찾을 수 없습니다."),
    TIME_SLOT_CLOSED("TS_002", 400, "이미 마감된 타임슬롯입니다."),
    NOT_ENOUGH_CAPACITY("TS_003", 400, "타임슬롯의 잔여 수용 인원이 부족합니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}