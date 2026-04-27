package com.michelet.timeslotservice.exception;

import com.michelet.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 타임슬롯 도메인에서 발생하는 비즈니스 예외 코드 모음.
 */
@Getter
@RequiredArgsConstructor
public enum TimeSlotErrorCode implements ErrorCode {
    
    /** 타임슬롯 조회 실패 에러 */
    TIME_SLOT_NOT_FOUND("TS_001", 404, "해당 타임슬롯을 찾을 수 없습니다."),

    /** 타임슬롯 마감 에러 */
    TIME_SLOT_CLOSED("TS_002", 400, "이미 마감된 타임슬롯입니다."),

    /** 수용 인원 부족 에러 */
    NOT_ENOUGH_CAPACITY("TS_003", 400, "타임슬롯의 잔여 수용 인원이 부족합니다."),

    /** 유효하지 않은 수용 인원 요청 에러 */
    INVALID_CAPACITY_REQUEST("TS_004", 400, "유효하지 않은 수용 인원 요청입니다. 요청된 인원 수는 1 이상이어야 합니다."),

    /** 수용 인원 음수 에러 */
    INVALID_CAPACITY("TS_005", 400, "수용 인원은 음수일 수 없습니다."),

    /** 유효하지 않은 시간 범위 에러 */
    INVALID_TIME_RANGE("TS_006", 400, "시작 시간은 종료 시간보다 이전이어야 합니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}