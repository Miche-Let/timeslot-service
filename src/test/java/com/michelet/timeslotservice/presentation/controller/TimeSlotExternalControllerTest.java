package com.michelet.timeslotservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michelet.common.auth.core.context.UserContext;
import com.michelet.common.auth.core.enums.UserRole;
import com.michelet.common.auth.webmvc.context.UserContextHolder;
import com.michelet.common.auth.webmvc.interceptor.UserContextInterceptor;
import com.michelet.common.exception.GlobalExceptionHandler;
import com.michelet.timeslotservice.application.service.TimeSlotService;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.TimeSlotStatus;
import com.michelet.timeslotservice.infrastructure.config.WebConfig;
import com.michelet.timeslotservice.presentation.dto.request.TimeSlotBulkCreateRequest;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * [External API Test]
 * 클라이언트 통신을 담당하는 TimeSlotExternalController의 동작을 검증합니다.
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
     * [GET] 특정 날짜의 타임슬롯 목록을 조회하면 DTO 리스트로 반환한다.
     * @throws Exception
     */
    @Test
    @DisplayName("[GET] 특정 날짜의 타임슬롯 목록을 조회하면 DTO 리스트로 반환한다.")
    void getTimeSlots_Success() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2036, 5, 5);
        TimeSlot mockSlot = TimeSlotTestBuilder.aTimeSlot().targetDate(date).build();

        given(timeSlotService.getTimeSlotsByDate(restaurantId, date))
                .willReturn(List.of(mockSlot));

        mockMvc.perform(get("/api/v1/restaurants/{restaurantId}/time-slots", restaurantId)
                        .param("targetDate", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].timeSlotId").value(mockSlot.getId().toString()));
    }

    /**
     * [POST] 일괄 생성 요청 시 DTO의 값을 낱개로 분해하여 서비스에 전달한다.
     * @throws Exception
     */
    @Test
    @DisplayName("[POST] 일괄 생성 요청 시 DTO의 값을 낱개로 분해하여 서비스에 전달한다.")
    void createTimeSlotsBulk_Success() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        TimeSlotBulkCreateRequest request = new TimeSlotBulkCreateRequest(
                LocalDate.of(2036, 5, 10), LocalDate.of(2036, 5, 15),
                LocalTime.of(9, 0), LocalTime.of(21, 0), 30, 4
        );

        mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/time-slots/bulk", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(timeSlotService).createTimeSlotsBulk(
                eq(restaurantId), eq(request.startDate()), eq(request.endDate()),
                eq(request.openTime()), eq(request.closeTime()), 
                eq(request.intervalMinutes()), eq(request.capacity())
        );
    }

    /**
     * [GET] 월간 달력 조회 시 서비스가 반환한 Map을 DTO 규격에 맞춰 응답한다.
     * @throws Exception
     */
    @Test
    @DisplayName("[GET] 월간 달력 조회 시 서비스가 반환한 Map을 DTO 규격에 맞춰 응답한다.")
    void getCalendarByMonth_Success() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2036, 5, 1);
        
        given(timeSlotService.getCalendarByMonth(any(), eq(2036), eq(5)))
                .willReturn(Map.of(date, TimeSlotStatus.OPENED));

        mockMvc.perform(get("/api/v1/restaurants/{restaurantId}/time-slots/calendar", restaurantId)
                        .param("year", "2036")
                        .param("month", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].date").value("2036-05-01"))
                .andExpect(jsonPath("$.data[0].status").value("OPENED"));
    }
}