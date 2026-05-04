package com.michelet.timeslotservice.domain.exception;

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

    /** 수용 인원 설정 오류 */
    INVALID_CAPACITY("TS_005", 400, "타임슬롯의 수용 인원 설정이 올바르지 않습니다."),
    
    /** 시간 범위 설정 오류 */
    INVALID_TIME_RANGE("TS_006", 400, "시작 시간은 종료 시간보다 앞서야 합니다."),

    /** 날짜 범위 설정 오류 */
    INVALID_DATE_RANGE("TS_007", 400, "시작일은 종료일보다 늦을 수 없습니다."),

    /** 중복된 타임슬롯 생성 시도 에러 */
    DUPLICATE_TIME_SLOT("TS_008", 409, "해당 시간에 이미 생성된 타임슬롯이 존재합니다."),

    /** 날짜 범위가 너무 큰 경우 에러 */
    DATE_RANGE_TOO_LARGE("TS_009", 400, "날짜 범위가 너무 큽니다. 최대 31일까지 허용됩니다."),

    /** 인증 실패 에러 */
    UNAUTHORIZED("TS_010", 401, "인증이 필요합니다."),

    /** 권한 부족 에러 */
    FORBIDDEN("TS_011", 403, "권한이 없습니다.");
    

    private final String code;
    private final int httpStatus;
    private final String message;
}