package com.michelet.timeslotservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michelet.common.auth.core.context.UserContext;
import com.michelet.common.auth.core.enums.UserRole;
import com.michelet.common.auth.webmvc.context.UserContextHolder;
import com.michelet.common.auth.webmvc.interceptor.UserContextInterceptor;
import com.michelet.common.exception.BusinessException;
import com.michelet.common.exception.GlobalExceptionHandler;
import com.michelet.timeslotservice.application.service.TimeSlotService;
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;
import com.michelet.timeslotservice.infrastructure.config.WebConfig;
import com.michelet.timeslotservice.presentation.dto.request.TimeSlotDeductCapacityRequest;
import com.michelet.timeslotservice.support.builder.TimeSlotTestBuilder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

/**
 * [Internal API Test]
 * 시스템 내부 통신(Server to Server)을 담당하는 TimeSlotInternalController의 동작을 검증합니다.
 */
@WebMvcTest(
    controllers = TimeSlotInternalController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class),
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
)
@Import(GlobalExceptionHandler.class)
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

    /**
     * 성공 케이스: 타임슬롯 정원 차감 요청이 정상적으로 처리되고, 서비스의 deductCapacity 메서드가 올바른 인자로 호출되는지 검증합니다.
     * @throws Exception
     */
    @Test
    @DisplayName("[Internal] 타임슬롯 정원 차감 요청이 성공적으로 처리된다.")
    void deductCapacity_Success() throws Exception {
        // given
        TimeSlotDeductCapacityRequest request = new TimeSlotDeductCapacityRequest(3);

        // when & then
        mockMvc.perform(post("/internal/v1/timeslots/{timeSlotId}/deduct", TimeSlotTestBuilder.DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
                
        then(timeSlotService).should().deductCapacity(eq(TimeSlotTestBuilder.DEFAULT_ID), eq(3));
    }

    /**
     * DTO 유효성 검사 (입력값이 0 이하일 때)
     * Request DTO의 @Valid(@Min(1) 등)가 정상 작동하는지 검증합니다.
     */
    @Test
    @DisplayName("[Internal] 차감 인원으로 0 이하의 값을 요청하면 400 Bad Request를 반환한다.")
    void deductCapacity_Fail_InvalidCapacity() throws Exception {
        // given
        TimeSlotDeductCapacityRequest invalidRequest = new TimeSlotDeductCapacityRequest(0);

        // when & then
        mockMvc.perform(post("/internal/v1/timeslots/{timeSlotId}/deduct", TimeSlotTestBuilder.DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
                
        then(timeSlotService).shouldHaveNoInteractions();
    }

    /**
     * 비즈니스 로직 예외 1 (남은 정원 부족)
     */
    @Test
    @DisplayName("[Internal] 남은 정원을 초과하여 차감을 요청하면 NOT_ENOUGH_CAPACITY 예외 응답을 반환한다.")
    void deductCapacity_Fail_ExceedCapacity() throws Exception {
        // given
        TimeSlotDeductCapacityRequest request = new TimeSlotDeductCapacityRequest(100);
        
        willThrow(new BusinessException(TimeSlotErrorCode.NOT_ENOUGH_CAPACITY))
                .given(timeSlotService).deductCapacity(eq(TimeSlotTestBuilder.DEFAULT_ID), eq(100));

        // when & then
        mockMvc.perform(post("/internal/v1/timeslots/{timeSlotId}/deduct", TimeSlotTestBuilder.DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(TimeSlotErrorCode.NOT_ENOUGH_CAPACITY.getCode()));
    }

    /**
     * 비즈니스 로직 예외 2 (타임슬롯 존재하지 않음)
     */
    @Test
    @DisplayName("[Internal] 존재하지 않는 타임슬롯 ID를 요청하면 TIME_SLOT_NOT_FOUND 예외 응답을 반환한다.")
    void deductCapacity_Fail_NotFound() throws Exception {
        // given
        UUID fakeId = UUID.randomUUID();
        TimeSlotDeductCapacityRequest request = new TimeSlotDeductCapacityRequest(2);
        
        willThrow(new BusinessException(TimeSlotErrorCode.TIME_SLOT_NOT_FOUND))
                .given(timeSlotService).deductCapacity(eq(fakeId), eq(2));

        // when & then
        mockMvc.perform(post("/internal/v1/timeslots/{timeSlotId}/deduct", fakeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(TimeSlotErrorCode.TIME_SLOT_NOT_FOUND.getCode()));
    }
}