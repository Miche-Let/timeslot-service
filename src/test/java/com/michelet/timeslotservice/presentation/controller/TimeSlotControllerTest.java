package com.michelet.timeslotservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michelet.timeslotservice.application.service.TimeSlotService;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.presentation.dto.request.TimeSlotDeductCapacityRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.michelet.timeslotservice.support.fixture.TimeSlotFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Presentation Layer(웹 계층)인 TimeSlotController의 HTTP 통신을 검증하는 슬라이스 테스트입니다.
 * <p>
 * {@code @WebMvcTest}를 사용하여 실제 웹 서버를 구동하지 않고 웹 계층에 관련된 빈(Bean)만 로드합니다.
 * 하위 계층인 Service는 {@code @MockitoBean}으로 가짜 객체(Mock) 처리하여, 
 * 오직 HTTP 요청 파싱(JSON 매핑)과 응답 규격(상태 코드 등)에만 집중하여 테스트 속도를 극대화합니다.
 * </p>
 */
@WebMvcTest(TimeSlotController.class)
class TimeSlotControllerTest {

    /**
     * 실제 네트워크 통신 없이 가상으로 HTTP 요청을 수행하고 응답을 검증하는 스프링의 핵심 테스트 도구입니다.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * 자바 객체(DTO)를 JSON 문자열로 변환하거나, 반대로 파싱하기 위한 Jackson 라이브러리의 매퍼입니다.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Controller가 의존하는 Service 계층을 완벽히 격리하기 위해 주입된 가짜(Mock) 빈입니다.
     */
    @MockitoBean
    private TimeSlotService timeSlotService;

    /**
     * GET 요청을 통해 타임슬롯 목록을 조회할 때, 파라미터 바인딩과 JSON 응답 포맷이 
     * 표준 ApiResponse 규격에 맞게 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("특정 식당/날짜의 타임슬롯 목록을 조회하면 200 상태코드와 ApiResponse 규격으로 반환된다.")
    void getTimeSlots_Success() throws Exception {
        // Given: 서비스 계층이 호출되었을 때 반환할 가짜 데이터 대본을 작성합니다.
        TimeSlot mockTimeSlot = createDomain(4, 4);

        given(timeSlotService.getTimeSlotsByDate(FIXTURE_RESTAURANT_ID, FIXTURE_DATE))
                .willReturn(List.of(mockTimeSlot));

        // When & Then: MockMvc를 이용해 가상의 GET HTTP 요청을 날리고,
        // HTTP 상태코드(200 OK) 및 JSON Path를 이용해 응답 본문의 구조를 꼼꼼히 검증합니다.
        mockMvc.perform(get("/api/v1/timeslots")
                        .param("restaurantId", FIXTURE_RESTAURANT_ID.toString())
                        .param("targetDate", FIXTURE_DATE.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)) // 공통 응답 규격의 성공 여부 확인
                .andExpect(jsonPath("$.code").value("TS_OK_001")) // 성공 비즈니스 코드 확인
                .andExpect(jsonPath("$.data[0].id").value(FIXTURE_ID.toString())); // 페이로드 검증
    }

    /**
     * POST 요청을 통해 인원 차감을 시도할 때, JSON Body 파싱이 정상적으로 이루어지고 
     * void 반환 타입의 비즈니스 로직 호출 후 성공 상태 코드가 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("타임슬롯 예약을 차감하면 200 상태코드와 성공 메시지가 반환된다.")
    void deductCapacity_Success() throws Exception {
        // Given: 클라이언트가 보낼 JSON Body용 DTO 객체를 생성합니다.
        TimeSlotDeductCapacityRequest request = new TimeSlotDeductCapacityRequest(2);
        
        // 반환 타입이 void인 서비스 메서드는 willDoNothing()을 사용하여 조용히 넘어가도록 대본을 짭니다.
        willDoNothing().given(timeSlotService).deductCapacity(eq(FIXTURE_ID), any(Integer.class));

        // When & Then: DTO를 JSON 문자열로 변환하여 POST 요청 Body에 담아 전송하고, 결과를 검증합니다.
        mockMvc.perform(post("/api/v1/timeslots/{timeSlotId}/deduct", FIXTURE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // 객체를 JSON 문자열로 직렬화
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("TS_OK_002"));
    }
}