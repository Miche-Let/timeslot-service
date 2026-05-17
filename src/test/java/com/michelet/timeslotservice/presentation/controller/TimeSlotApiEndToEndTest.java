package com.michelet.timeslotservice.presentation.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.repository.TimeSlotRepository;
import com.michelet.timeslotservice.support.IntegrationTestSupport;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

import static com.michelet.timeslotservice.support.builder.TimeSlotTestBuilder.aTimeSlot;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * [E2E Test] 실제 톰캣 서버를 띄우고 클라이언트 관점에서 API 흐름을 검증합니다.
 */

class TimeSlotApiEndToEndTest extends IntegrationTestSupport {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    /**
     * [E2E] 잘못된 날짜로 달력 조회를 요청하면 HTTP 400 에러와 함께 공통 에러 포맷이 반환된다.
     */
    @Test
    @DisplayName("[E2E] 잘못된 날짜로 달력 조회를 요청하면 에러와 함께 공통 에러 포맷이 반환된다.")
    void getCalendar_E2E_Fail_InvalidParameter() {
        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/restaurants/123e4567-e89b-12d3-a456-426614174000/time-slots/calendar?year=2026&month=13",
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("\"code\":\"INTERNAL_001\"");
    }

    /**
     * [E2E] 정상적인 연/월로 달력을 조회하면 HTTP 200 OK가 반환된다.
     */
    @Test
    @DisplayName("[E2E] 정상적인 연/월로 달력을 조회하면 HTTP 200 OK가 반환된다.")
    void getCalendar_E2E_Success() {
        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/restaurants/123e4567-e89b-12d3-a456-426614174000/time-slots/calendar?year=2026&month=5",
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /*
     * [E2E] POST 요청 시 필수 바디 값이 누락되면 서버가 차단한다.
     */
    @Test
    @DisplayName("[E2E] POST 요청 시 필수 바디 값이 누락되면 서버가 차단한다.")
    void createTimeSlotsBulk_E2E_Fail_Validation() {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String invalidJsonBody = """
                {
                    "intervalMinutes": 60,
                    "capacity": 4
                }
                """;
        
        HttpEntity<String> request = new HttpEntity<>(invalidJsonBody, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/restaurants/123e4567-e89b-12d3-a456-426614174000/time-slots/bulk",
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("\"code\":\"VALIDATION_001\""); 
    }


    /**
     * [E2E] 내부 서비스(결제 등)에서 타임슬롯 인원 복원(PATCH)을 요청하면 정상 처리된다.
     */
    @Test
    @DisplayName("[E2E] 타임슬롯 인원 복원 요청 시 HTTP 200 OK가 반환된다.")
    void restoreCapacity_E2E_Success() {
        // given
        TimeSlot timeSlot = aTimeSlot()
                .id(null)
                .restaurantId(java.util.UUID.randomUUID())
                .targetDate(java.time.LocalDate.of(2099, 5, 20))
                .time(java.time.LocalTime.of(14, 0), java.time.LocalTime.of(15, 0))
                .capacity(4)
                .remainingCapacity(2)
                .build();

        TimeSlot savedSlot = timeSlotRepository.save(timeSlot);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("X-Internal-Token", createTestInternalToken());
        
        String requestBody = "{\"restoreCapacity\": 1}";
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        String url = "/internal/v1/time-slots/" + savedSlot.getId() + "/restore";

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                request,
                String.class
        );
        System.out.println("응답 상태 코드: " + response.getStatusCode());
        System.out.println("응답 바디: " + response.getBody());
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}