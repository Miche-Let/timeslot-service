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
import com.michelet.timeslotservice.domain.exception.TimeSlotErrorCode;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = TimeSlotExternalController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
)
@Import(GlobalExceptionHandler.class)
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

	/**
	 * 특정 식당의 특정 일자 타임슬롯 목록 조회 API
	 * @throws Exception
	 */
	@Test
	@DisplayName("[External] 특정 식당의 특정 일자 타임슬롯 목록 조회 API")
	void getTimeSlots_Success() throws Exception {
	UUID restaurantId = UUID.randomUUID();
	UUID timeslotId = UUID.randomUUID();
	LocalDate targetDate = LocalDate.of(2036, 5, 1);

	List<TimeSlot> mockSlots = List.of(
			TimeSlotTestBuilder.aTimeSlot()
					.id(timeslotId)
					.restaurantId(restaurantId)
					.targetDate(targetDate)
					.capacity(4)
					.remainingCapacity(2)
					.status(TimeSlotStatus.OPENED)
					.build(),
			TimeSlotTestBuilder.aTimeSlot()
					.id(timeslotId)
					.restaurantId(restaurantId)
					.targetDate(targetDate)
					.capacity(4)
					.remainingCapacity(4)
					.status(TimeSlotStatus.OPENED)
					.build()
	);
	// given
	given(timeSlotService.getTimeSlotsByDate(eq(restaurantId), eq(targetDate)))
			.willReturn(mockSlots);

	// when & then
	mockMvc.perform(get("/api/v1/restaurants/{restaurantId}/time-slots", restaurantId)
					.param("targetDate", targetDate.toString())
					.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}

	/**
	 * 특정 식당의 특정 일자 타임슬롯 목록 조회 API - 빈 리스트 반환 케이스
	 * @throws Exception
	 */
	@Test
	@DisplayName("[External] 특정 식당의 특정 일자의 타임슬롯이 존재하지 않을 때 빈 리스트를 반환한다.")
	void getTimeSlots_EmptyList() throws Exception {
	UUID restaurantId = UUID.randomUUID();
	LocalDate targetDate = LocalDate.of(2036, 5, 1);

	// given
	given(timeSlotService.getTimeSlotsByDate(eq(restaurantId), eq(targetDate)))
			.willReturn(List.of());

	// when & then
	mockMvc.perform(get("/api/v1/restaurants/{restaurantId}/time-slots", restaurantId)
					.param("targetDate", targetDate.toString())
					.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}


	/**
	 * 특정 식당의 타임슬롯을 일괄 생성하는 API - 성공 케이스
	 * @throws Exception
	 */
	@Test
	@DisplayName("[External] 특정 식당의 일괄 타임슬롯 생성 API - 성공 케이스")
	void createTimeSlotsBulk_Success() throws Exception{
	UUID restaurantId = UUID.randomUUID();

	// given
	TimeSlotBulkCreateRequest request = new TimeSlotBulkCreateRequest(
			LocalDate.of(2036, 5, 1),
			LocalDate.of(2036, 5, 7),
					LocalTime.of(10, 0),
					LocalTime.of(20,0),
			60,
			4
	);

	// when & then
	mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/time-slots/bulk", restaurantId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());
	}


	/**
	 * 특정 식당의 타임슬롯에 1달이 넘는 기간을 요청할 때, 400 Bad Request를 반환하는 케이스
	 * @throws Exception
	 */

	@Test
	@DisplayName("[External] 특정 식당의 일괄 타임슬롯 생성 API - 1달이 넘는 기간을 요청하면 400 Bad Request를 반환한다.")
	void createTimeSlotsBulk_Fail_InvalidDateRange() throws Exception{
		UUID restaurantId = UUID.randomUUID();

		// given
		TimeSlotBulkCreateRequest request = new TimeSlotBulkCreateRequest(
				LocalDate.of(2036, 5, 1),
				LocalDate.of(2036, 6, 15),
				LocalTime.of(10, 0),
				LocalTime.of(20,0),
				60,
				4
		);

		// when & then
		mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/time-slots/bulk", restaurantId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(TimeSlotErrorCode.DATE_RANGE_TOO_LARGE.getCode()));

	}

	/**
     * [External] JSR-380 Validation 검증
     * DTO의 필수 값이 누락되었을 때, GlobalExceptionHandler가 MethodArgumentNotValidException을 잡아
     * 400 Bad Request와 함께 우리가 약속한 VALIDATION_001 에러 규격을 반환하는지 검증합니다.
     */
    @Test
    @DisplayName("[External] 일괄 생성 시 필수 값(closeTime)이 누락되면 400 Bad Request와 VALIDATION_001 규격을 반환한다.")
    void createTimeSlotsBulk_Fail_MissingRequiredField() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        // given
        TimeSlotBulkCreateRequest request = new TimeSlotBulkCreateRequest(
                LocalDate.of(2036, 5, 1),
                LocalDate.of(2036, 5, 15),
                LocalTime.of(10, 0),
                null,
                60,
                4
        );

        // when & then
        mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/time-slots/bulk", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) 
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_001"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("영업 종료 시간은 필수입니다"))); 
                
        then(timeSlotService).shouldHaveNoInteractions();
    }

	/**
	 * 특정 식당의 월간 달력 조회 API - 성공 케이스
	 * @throws Exception
	 */
	@Test
	@DisplayName("[External] 특정 식당의 월간 달력 조회 API - 성공 케이스")
	void getCalendarByMonth_Success() throws Exception {
		UUID restaurantId = UUID.randomUUID();
		int year = 2036;
		int month = 5;

		// given
		Map<LocalDate, TimeSlotStatus> mockServiceResponse = Map.of(
                LocalDate.of(2026, 5, 25), TimeSlotStatus.OPENED,
                LocalDate.of(2026, 5, 26), TimeSlotStatus.CLOSED
        );

		given(timeSlotService.getCalendarByMonth(eq(restaurantId), eq(year), eq(month)))
                .willReturn(mockServiceResponse);

		// when & then
		mockMvc.perform(get("/api/v1/restaurants/{restaurantId}/time-slots/calendar", restaurantId)
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].date").value("2026-05-25"))
                .andExpect(jsonPath("$.data[0].status").value("OPENED"))
                .andExpect(jsonPath("$.data[1].date").value("2026-05-26"))
                .andExpect(jsonPath("$.data[1].status").value("CLOSED"));
    }

	/**
	 * [External] 월간 달력 조회 시 2024년 미만의 값을 요청하면 400 Bad Request를 반환하는 케이스
	 * @throws Exception
	 */
    @Test
    @DisplayName("[External] 월간 달력 조회 시 2024년 미만의 값을 요청하면 400 Bad Request를 반환한다.")
    void getCalendarByMonth_Fail_InvalidYear() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        // when & then
        mockMvc.perform(get("/api/v1/restaurants/{restaurantId}/time-slots/calendar", restaurantId)
                        .param("year", "2023")
                        .param("month", "5")
                        .accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INTERNAL_001"));
                
        then(timeSlotService).shouldHaveNoInteractions();
    }


	/**
	 * [External] 월간 달력 조회 시 12월을 초과하는 값을 요청하면 400 Bad Request를 반환하는 케이스
	 * @throws Exception
	 */
	@Test
    @DisplayName("[External] 월간 달력 조회 시 12월을 초과하는 값을 요청하면 400 Bad Request를 반환한다.")
    void getCalendarByMonth_Fail_InvalidMonth() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        // when & then
        mockMvc.perform(get("/api/v1/restaurants/{restaurantId}/time-slots/calendar", restaurantId)
                        .param("year", "2026")
                        .param("month", "13")
                        .accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INTERNAL_001"));
                
        then(timeSlotService).shouldHaveNoInteractions();
    }
}