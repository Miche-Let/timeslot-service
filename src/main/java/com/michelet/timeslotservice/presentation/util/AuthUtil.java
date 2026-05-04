package com.michelet.timeslotservice.presentation.util;

import com.michelet.common.auth.core.enums.UserRole;
import com.michelet.common.auth.webmvc.context.UserContextHolder;
import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;

public final class AuthUtil {
    private AuthUtil() {}

    /**
     * 관리자 권한(OWNER, MASTER)이 있는지 확인합니다.
     * 권한이 없으면 즉시 403 Forbidden 예외를 발생시킵니다.
     */
    public static void verifyManagerRole() {
        var context = UserContextHolder.get();
        if (context == null || context.role() == null) {
            throw new BusinessException(TimeSlotErrorCode.UNAUTHORIZED);
        }

        UserRole role = context.role();
        if (role != UserRole.OWNER && role != UserRole.MASTER) {
            throw new BusinessException(TimeSlotErrorCode.FORBIDDEN);
        }
    }
}