package com.michelet.timeslotservice.presentation.controller;

import com.michelet.timeslotservice.application.service.TimeSlotService;
import com.michelet.timeslotservice.domain.TimeSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.michelet.timeslotservice.support.fixture.TimeSlotFixture.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    @DisplayName("[External] 특정 식당/날짜의 타임슬롯 목록을 조회하면 200 상태코드와 규격에 맞게 반환된다.")
    void getTimeSlots_Success() throws Exception {
        // Given
        TimeSlot mockTimeSlot = createDomain(4, 4);

        given(timeSlotService.getTimeSlotsByDate(FIXTURE_RESTAURANT_ID, FIXTURE_DATE))
                .willReturn(List.of(mockTimeSlot));

        // When & Then
        mockMvc.perform(get("/api/v1/timeslots")
                        .param("restaurantId", FIXTURE_RESTAURANT_ID.toString())
                        .param("targetDate", FIXTURE_DATE.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("TS_OK_001"))
                .andExpect(jsonPath("$.data[0].id").value(FIXTURE_ID.toString()));
    }
}