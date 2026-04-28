package com.michelet.timeslotservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michelet.timeslotservice.application.service.TimeSlotService;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.TimeSlotStatus;
import com.michelet.timeslotservice.presentation.dto.request.TimeSlotDeductCapacityRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // ⭐ Spring Boot 3.4+ 최신 어노테이션
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TimeSlotController의 웹 계층(Presentation Layer)을 독립적으로 검증하는 슬라이스 테스트(Slice Test) 클래스입니다.
 * <p>
 * 비즈니스 로직(Service)은 MockitoBean으로 대체(Mocking)하여,
 * HTTP 요청/응답 라우팅, JSON 직렬화/역직렬화, HTTP 상태 코드 및 공통 응답 규격(ApiResponse)이 
 * API 명세서대로 정확히 동작하는지 검증하는 데 목적이 있습니다.
 * </p>
 */
@WebMvcTest(TimeSlotController.class)
class TimeSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 비즈니스 로직의 실제 동작을 배제하고, 컨트롤러의 동작만 테스트하기 위한 가짜(Mock) 객체
    @MockitoBean
    private TimeSlotService timeSlotService;

    /**
     * [조회 API 테스트]
     * 클라이언트가 올바른 파라미터(restaurantId, targetDate)를 전달했을 때,
     * 컨트롤러가 서비스 계층을 정상 호출하고, 반환된 데이터를 공통 ApiResponse 포맷(200 OK)으로 응답하는지 검증합니다.
     */
    @Test
    @DisplayName("특정 식당/날짜의 타임슬롯 목록을 조회하면 200 상태코드와 ApiResponse 규격으로 반환된다.")
    void getTimeSlots_Success() throws Exception {
        // Given (테스트를 위한 사전 데이터 세팅)
        UUID restaurantId = UUID.randomUUID();
        LocalDate targetDate = LocalDate.of(2026, 5, 2);
        UUID timeSlotId = UUID.randomUUID();
        
        TimeSlot mockTimeSlot = TimeSlot.builder()
                .id(timeSlotId)
                .restaurantId(restaurantId)
                .targetDate(targetDate)
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(13, 0))
                .capacity(4)
                .remainingCapacity(4)
                .status(TimeSlotStatus.OPENED)
                .build();

        // 서비스가 호출되면 가짜 데이터(mockTimeSlot)를 반환하도록 조작
        when(timeSlotService.getTimeSlotsByDate(restaurantId, targetDate))
                .thenReturn(List.of(mockTimeSlot));

        // When & Then (실제 API 호출 및 결과 검증)
        mockMvc.perform(get("/api/v1/timeslots")
                        .param("restaurantId", restaurantId.toString())
                        .param("targetDate", targetDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("TS_OK_001"))
                .andExpect(jsonPath("$.data[0].id").value(timeSlotId.toString()));
    }

    /**
     * [차감 API 테스트]
     * 클라이언트가 올바른 JSON Body(DeductRequest)를 전달했을 때,
     * 컨트롤러가 정상적으로 파싱하여 서비스를 호출하고, 성공 응답(200 OK)을 반환하는지 검증합니다.
     */
    @Test
    @DisplayName("타임슬롯 예약을 차감하면 200 상태코드와 성공 메시지가 반환된다.")
    void deductCapacity_Success() throws Exception {
        // Given (테스트를 위한 사전 데이터 세팅)
        UUID timeSlotId = UUID.randomUUID();
        TimeSlotDeductCapacityRequest request = new TimeSlotDeductCapacityRequest(2);
        
        // 서비스 메서드가 void이므로 호출 시 아무 일도 하지 않도록 설정
        doNothing().when(timeSlotService).deductCapacity(eq(timeSlotId), any(Integer.class));

        // When & Then (실제 API 호출 및 결과 검증)
        mockMvc.perform(post("/api/v1/timeslots/{timeSlotId}/deduct", timeSlotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("TS_OK_002"));
    }
}