package com.michelet.timeslotservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.application.service.TimeSlotService;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.presentation.dto.request.TimeSlotBulkCreateRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.michelet.timeslotservice.support.fixture.TimeSlotFixture.*;
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
 * <p>
 * 타임슬롯 목록 조회, 생성 요청 등 사용자에게 노출되는 퍼블릭 엔드포인트(/api/v1/...)가
 * 프론트엔드가 기대하는 Response DTO 규격(데이터 포맷, 공통 응답 구조)에 맞게 
 * 데이터를 잘 변환하여 내려주는지 집중적으로 확인하는 슬라이스 테스트입니다.
 * </p>
 */
@WebMvcTest(TimeSlotExternalController.class)
class TimeSlotExternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeSlotService timeSlotService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("[External] 특정 식당/날짜의 타임슬롯 목록을 조회하면 200 상태코드와 규격에 맞게 반환된다.")
    void getTimeSlots_Success() throws Exception {

        TimeSlot mockTimeSlot = createDomain(4, 4);

        given(timeSlotService.getTimeSlotsByDate(FIXTURE_RESTAURANT_ID, FIXTURE_DATE))
                .willReturn(List.of(mockTimeSlot));

        mockMvc.perform(get("/api/v1/timeslots")
                        .param("restaurantId", FIXTURE_RESTAURANT_ID.toString())
                        .param("targetDate", FIXTURE_DATE.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("TS_OK_001"))
                .andExpect(jsonPath("$.data[0].id").value(FIXTURE_ID.toString()));
    }

    /**
     * 프론트엔드의 타임슬롯 일괄 생성(POST) 요청이 
     * 적절한 URL 경로를 타고 들어와 정상 응답(200 OK)을 반환하는지 검증합니다.
     */
    @Test
    @DisplayName("[External] 타임슬롯 일괄 생성 요청 시 200 상태코드와 성공 응답 규격이 반환된다.")
    void createTimeSlotsBulk_Success() throws Exception {
        TimeSlotBulkCreateRequest request = new TimeSlotBulkCreateRequest(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 5),
                LocalTime.of(9, 0),
                LocalTime.of(21, 0),
                30,
                4
        );

        willDoNothing().given(timeSlotService).createTimeSlotsBulk(eq(FIXTURE_RESTAURANT_ID), any(TimeSlotBulkCreateRequest.class));

        mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/time-slots/bulk", FIXTURE_RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("TS_OK_003")); 
    }

    /**
     * DTO 내부의 논리적 모순(시작일이 종료일보다 늦은 경우)이 
     * Controller 진입 시점에서 잘 차단되는지 예외를 검증합니다.
     */
    @Test
    @DisplayName("[External] 시작일이 종료일보다 늦으면 Controller에서 예외가 발생한다.")
    void createTimeSlotsBulk_Fail_InvalidDateRange() throws Exception {
        TimeSlotBulkCreateRequest invalidRequest = new TimeSlotBulkCreateRequest(
                LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 1),
                LocalTime.of(9, 0), LocalTime.of(21, 0), 30, 4
        );
        
        assertThatThrownBy(() ->
                mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/time-slots/bulk", FIXTURE_RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
        )
        .isInstanceOf(Exception.class)
        .hasCauseInstanceOf(BusinessException.class);
    }

    /**
     * DTO 내부의 논리적 모순(오픈 시간이 마감 시간보다 늦은 경우)이 
     * Controller 진입 시점에서 잘 차단되는지 예외를 검증합니다.
     */
    @Test
    @DisplayName("[External] 시작 시간이 종료 시간보다 늦으면 Controller에서 예외가 발생한다.")
    void createTimeSlotsBulk_Fail_InvalidTimeRange() throws Exception {

        TimeSlotBulkCreateRequest invalidRequest = new TimeSlotBulkCreateRequest(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 5),
                LocalTime.of(18, 0), LocalTime.of(17, 0),
                30, 4
        );

        assertThatThrownBy(() ->
                mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/time-slots/bulk", FIXTURE_RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
        )
        .isInstanceOf(Exception.class)
        .hasCauseInstanceOf(BusinessException.class);
    }


        /**
        * DTO 내부의 논리적 모순(날짜 범위가 너무 큰 경우)이 
        * Controller 진입 시점에서 잘 차단되는지 예외를 검증합니다.
        */
    @Test
    @DisplayName("[External] 날짜 범위가 너무 크면 Controller에서 예외가 발생한다.")
    void createTimeSlotsBulk_Fail_DateRangeTooLarge() throws Exception {

        TimeSlotBulkCreateRequest invalidRequest = new TimeSlotBulkCreateRequest(
                LocalDate.of(2026, 5, 1), LocalDate.of(2036, 6, 1),
                LocalTime.of(9, 0), LocalTime.of(21, 0),
                30, 4
        );

        assertThatThrownBy(() ->
                mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/time-slots/bulk", FIXTURE_RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
        )
        .isInstanceOf(Exception.class)
        .hasCauseInstanceOf(BusinessException.class);
    }
}