package com.michelet.timeslotservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michelet.common.auth.core.context.UserContext;
import com.michelet.common.auth.core.enums.UserRole;
import com.michelet.common.auth.webmvc.context.UserContextHolder;
import com.michelet.common.auth.webmvc.interceptor.UserContextInterceptor;
import com.michelet.common.exception.BusinessException;
import com.michelet.common.exception.GlobalExceptionHandler;
import com.michelet.timeslotservice.application.service.TimeSlotService;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.infrastructure.config.WebConfig;
import com.michelet.timeslotservice.presentation.dto.request.TimeSlotBulkCreateRequest;
import com.michelet.timeslotservice.presentation.dto.response.TimeSlotCalendarResponse;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * [External API Test]
 * 클라이언트(프론트엔드/웹/앱)와 직접 통신하는 TimeSlotExternalController의 동작을 검증합니다.
 */
@WebMvcTest(
    controllers = TimeSlotExternalController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class),
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
class TimeSlotExternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeSlotService timeSlotService;

    @Autowired
    private ObjectMapper objectMapper;

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
     * [External API Test] 특정 식당과 날짜의 타임슬롯 목록 조회 성공 케이스
     * @throws Exception
     */

    @Test
    @DisplayName("[External] 특정 식당/날짜의 타임슬롯 목록을 조회하면 200 상태코드와 규격에 맞게 반환된다.")
    void getTimeSlots_Success() throws Exception {
        
        TimeSlot mockTimeSlot = TimeSlotTestBuilder.aTimeSlot().build();

        given(timeSlotService.getTimeSlotsByDate(TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID, TimeSlotTestBuilder.DEFAULT_DATE))
                .willReturn(List.of(mockTimeSlot));

        mockMvc.perform(get("/api/v1/restaurants/{restaurantId}/time-slots", TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID)
                        .param("targetDate", TimeSlotTestBuilder.DEFAULT_DATE.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("TS_OK_001"))
                .andExpect(jsonPath("$.data[0].timeSlotId").value(TimeSlotTestBuilder.DEFAULT_ID.toString()))
                .andExpect(jsonPath("$.data[0].remainingCapacity").value(4))
                .andExpect(jsonPath("$.data[0].status").value("OPENED"));
    }

    /**
     * [External API Test] 타임슬롯 일괄 생성 성공 케이스
     * @throws Exception
     */
    @Test
    @DisplayName("[External] 타임슬롯 일괄 생성 요청 시 200 상태코드와 성공 응답 규격이 반환된다.")
    void createTimeSlotsBulk_Success() throws Exception {
        TimeSlotBulkCreateRequest request = new TimeSlotBulkCreateRequest(
            LocalDate.of(2099, 5, 10),
            LocalDate.of(2099, 5, 15),
            LocalTime.of(9, 0),
            LocalTime.of(21, 0),
            30,
            4
        );

        willDoNothing().given(timeSlotService).createTimeSlotsBulk(eq(TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID), any(TimeSlotBulkCreateRequest.class));

        mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/time-slots/bulk", TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("TS_OK_003")); 
    }

    /**
     * [External API Test] 타임슬롯 일괄 생성 실패 케이스 - 날짜 범위 오류
     * @throws Exception
     */
    @Test
    @DisplayName("[External] 시작일이 종료일보다 늦으면 Controller에서 예외가 발생한다.")
    void createTimeSlotsBulk_Fail_InvalidDateRange() throws Exception {
        TimeSlotBulkCreateRequest invalidRequest = new TimeSlotBulkCreateRequest(
                LocalDate.of(2099, 5, 5), LocalDate.of(2099, 5, 1),
                LocalTime.of(9, 0), LocalTime.of(21, 0), 30, 4
        );
        
        assertThatThrownBy(() ->
                mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/time-slots/bulk", TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
        )
        .isInstanceOf(Exception.class)
        .hasCauseInstanceOf(BusinessException.class);
    }


    /**
     * [External API Test] 타임슬롯 일괄 생성 실패 케이스 - 시간 범위 오류
     * @throws Exception
     */
    @Test
    @DisplayName("[External] 시작 시간이 종료 시간보다 늦으면 Controller에서 예외가 발생한다.")
    void createTimeSlotsBulk_Fail_InvalidTimeRange() throws Exception {

        TimeSlotBulkCreateRequest invalidRequest = new TimeSlotBulkCreateRequest(
                LocalDate.of(2099, 5, 1), LocalDate.of(2099, 5, 5),
                LocalTime.of(18, 0), LocalTime.of(17, 0),
                30, 4
        );

        assertThatThrownBy(() ->
                mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/time-slots/bulk", TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
        )
        .isInstanceOf(Exception.class)
        .hasCauseInstanceOf(BusinessException.class);
    }

    /**
     * [External API Test] 타임슬롯 일괄 생성 실패 케이스 - 날짜 범위가 너무 큰 경우
     * @throws Exception
     */
    @Test
    @DisplayName("[External] 날짜 범위가 너무 크면 Controller에서 예외가 발생한다.")
    void createTimeSlotsBulk_Fail_DateRangeTooLarge() throws Exception {

        TimeSlotBulkCreateRequest invalidRequest = new TimeSlotBulkCreateRequest(
                LocalDate.of(2099, 5, 1), LocalDate.of(2099, 6, 22),
                LocalTime.of(18, 0), LocalTime.of(21, 0),
                30, 4
        );

        assertThatThrownBy(() ->
                mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/time-slots/bulk", TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
        )
        .isInstanceOf(Exception.class)
        .hasCauseInstanceOf(BusinessException.class);
    }

    /**
     * [External API Test] 특정 식당의 월간 달력 조회 성공 케이스
     * @throws Exception
     */
    @Test
    @DisplayName("[External] 특정 식당의 월간 달력 조회 시 200 상태코드와 규격에 맞는 데이터가 반환된다.")
    void getCalendarByMonth_Success() throws Exception {
        int year = 2099;
        int month = 5;
        
        TimeSlotCalendarResponse responseDay1 = new TimeSlotCalendarResponse(LocalDate.of(year, month, 1), "OPENED");
        TimeSlotCalendarResponse responseDay2 = new TimeSlotCalendarResponse(LocalDate.of(year, month, 2), "CLOSED");

        given(timeSlotService.getCalendarByMonth(TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID, year, month))
                .willReturn(List.of(responseDay1, responseDay2));

        mockMvc.perform(get("/api/v1/restaurants/{restaurantId}/time-slots/calendar", TimeSlotTestBuilder.DEFAULT_RESTAURANT_ID)
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("TS_OK_001"))
                .andExpect(jsonPath("$.data[0].date").value("2099-05-01"))
                .andExpect(jsonPath("$.data[0].status").value("OPENED"))
                .andExpect(jsonPath("$.data[1].date").value("2099-05-02"))
                .andExpect(jsonPath("$.data[1].status").value("CLOSED"));
    }
}