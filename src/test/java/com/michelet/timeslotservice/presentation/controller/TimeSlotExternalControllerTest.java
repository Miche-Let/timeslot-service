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
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = TimeSlotExternalController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class),
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs 
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

    @Test
    @DisplayName("[GET] 특정 날짜의 타임슬롯 목록 조회 문서화")
    void getTimeSlots_Docs() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2036, 5, 5);
        TimeSlot mockSlot = TimeSlotTestBuilder.aTimeSlot().targetDate(date).build();

        given(timeSlotService.getTimeSlotsByDate(restaurantId, date)).willReturn(List.of(mockSlot));

        mockMvc.perform(get("/api/v1/restaurants/{restaurantId}/time-slots", restaurantId)
                        .param("targetDate", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("timeslot-get-list", 
                        pathParameters(
                                parameterWithName("restaurantId").description("식당 고유 ID")
                        ),
                        queryParameters(
                                parameterWithName("targetDate").description("조회 대상 날짜 (YYYY-MM-DD)")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("success").description("요청 성공 여부"),
                                fieldWithPath("data[].timeSlotId").description("타임슬롯 ID"),
                                fieldWithPath("data[].startTime").description("시작 시간"),
                                fieldWithPath("data[].endTime").description("종료 시간"),
                                fieldWithPath("data[].capacity").description("최대 수용 인원"),
                                fieldWithPath("data[].remainingCapacity").description("잔여 수용 인원"),
                                fieldWithPath("data[].status").description("상태 (OPENED, CLOSED)")
                        )
                ));
    }

    @Test
    @DisplayName("[POST] 타임슬롯 일괄 생성 문서화")
    void createTimeSlotsBulk_Docs() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        TimeSlotBulkCreateRequest request = new TimeSlotBulkCreateRequest(
                LocalDate.of(2036, 5, 10), LocalDate.of(2036, 5, 15),
                LocalTime.of(9, 0), LocalTime.of(21, 0), 30, 4
        );

        mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/time-slots/bulk", restaurantId)
                        .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440000")
                        .header("X-User-Role", "OWNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("timeslot-create-bulk",
                        pathParameters(
                                parameterWithName("restaurantId").description("식당 고유 ID")
                        ),
                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 고유 ID (인증)"),
                                headerWithName("X-User-Role").description("사용자 권한 (OWNER 필수)")
                        ),
                        relaxedRequestFields(
                                fieldWithPath("startDate").description("생성 시작 일자"),
                                fieldWithPath("endDate").description("생성 종료 일자"),
                                fieldWithPath("openTime").description("영업 시작 시간"),
                                fieldWithPath("closeTime").description("영업 종료 시간"),
                                fieldWithPath("intervalMinutes").description("타임슬롯 단위(분)"),
                                fieldWithPath("capacity").description("타임슬롯 당 수용 인원")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("success").description("요청 성공 여부"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("[GET] 월간 달력 조회 문서화")
    void getCalendarByMonth_Docs() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2036, 5, 1);
        
        given(timeSlotService.getCalendarByMonth(any(), eq(2036), eq(5)))
                .willReturn(Map.of(date, TimeSlotStatus.OPENED));

        mockMvc.perform(get("/api/v1/restaurants/{restaurantId}/time-slots/calendar", restaurantId)
                        .param("year", "2036")
                        .param("month", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("timeslot-get-calendar",
                        pathParameters(
                                parameterWithName("restaurantId").description("식당 고유 ID")
                        ),
                        queryParameters(
                                parameterWithName("year").description("조회 연도 (예: 2036)"),
                                parameterWithName("month").description("조회 월 (1~12)")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("success").description("요청 성공 여부"),
                                fieldWithPath("data[].date").description("날짜 (YYYY-MM-DD)"),
                                fieldWithPath("data[].status").description("해당 일자 예약 가능 여부 (true: OPENED, false: CLOSED)")
                        )
                ));
    }
}