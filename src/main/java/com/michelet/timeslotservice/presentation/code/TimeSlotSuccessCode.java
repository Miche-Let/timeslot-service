package com.michelet.timeslotservice.presentation.code;

import com.michelet.common.response.SuccessCode;

/**
 * 타임슬롯 관련 API 성공 응답 코드 모음.
 */
public enum TimeSlotSuccessCode implements SuccessCode {
    
    INQUIRY_SUCCESS("TS_OK_001", "타임슬롯 조회가 완료되었습니다."),
    DEDUCT_SUCCESS("TS_OK_002", "타임슬롯 예약 차감이 완료되었습니다.");

    private final String code;
    private final String message;

    TimeSlotSuccessCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}