package com.michelet.timeslotservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michelet.common.auth.core.context.UserContext;
import com.michelet.common.auth.core.enums.UserRole;
import com.michelet.common.auth.webmvc.context.UserContextHolder;
import com.michelet.common.auth.webmvc.interceptor.UserContextInterceptor;
import com.michelet.common.exception.GlobalExceptionHandler;
import com.michelet.timeslotservice.application.service.TimeSlotService;
import com.michelet.timeslotservice.infrastructure.config.WebConfig;
import com.michelet.timeslotservice.presentation.dto.request.TimeSlotDeductCapacityRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.michelet.timeslotservice.support.fixture.TimeSlotFixture.FIXTURE_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

/**
 * [Internal API Test]
 * 시스템 내부 통신(Server to Server)을 담당하는 TimeSlotInternalController의 동작을 검증합니다.
 * <p>
 * 예약 차감(deduct)과 같이 외부로 노출되어서는 안 되는 핵심 변경 로직들이 
 * 올바른 경로(/internal/v1/...)를 통해 내부 규격에 맞게 작동하는지 확인하는 슬라이스 테스트입니다.
 * </p>
 */
@WebMvcTest(
    controllers = TimeSlotInternalController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class),
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
class TimeSlotInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TimeSlotService timeSlotService;

    @MockitoBean
    private UserContextInterceptor userContextInterceptor;

    @BeforeEach
    void setUp() {
        UserContext mockContext = new UserContext(UUID.randomUUID().toString(), UserRole.OWNER);
        UserContextHolder.set(mockContext);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("[Internal] 타임슬롯 예약 인원을 차감하면 정상 응답을 반환한다.")
    void deductCapacity_Success() throws Exception {
        // Given
        TimeSlotDeductCapacityRequest request = new TimeSlotDeductCapacityRequest(2);
        willDoNothing().given(timeSlotService).deductCapacity(eq(FIXTURE_ID), any(Integer.class));

        // When & Then
        mockMvc.perform(post("/internal/v1/timeslots/{timeSlotId}/deduct", FIXTURE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("TS_OK_002"));
    }
}