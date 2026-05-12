package com.michelet.timeslotservice.application.service;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.repository.TimeSlotRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.michelet.timeslotservice.support.builder.TimeSlotTestBuilder.aTimeSlot;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
class TimeSlotServiceIntegrationTest {

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @MockitoBean
    private AuditorAware<UUID> auditorAware;

    @BeforeEach
    void setUp() {
        given(auditorAware.getCurrentAuditor()).willReturn(Optional.of(UUID.randomUUID()));
    }

    /**
     * [통합] 1자리 남은 타임슬롯에 100명이 동시에 예약을 요청하면 1명만 성공하고 99명은 실패해야 한다.
     * @throws InterruptedException
     */
    @Test
    @DisplayName("[통합] 1자리 남은 타임슬롯에 100명이 동시에 예약을 요청하면 1명만 성공하고 99명은 실패해야 한다.")
    void deductCapacity_ConcurrencyTest() throws InterruptedException {
        // given
        TimeSlot slot = aTimeSlot()
                .id(null) 
                .restaurantId(UUID.randomUUID())
                .capacity(4)
                .remainingCapacity(1)
                .build();

        TimeSlot savedSlot = timeSlotRepository.save(slot);

        // when 
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    timeSlotService.deductCapacity(savedSlot.getId(), 1);
                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException | BusinessException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        TimeSlot updatedSlot = timeSlotRepository.findById(savedSlot.getId()).orElseThrow();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(99);
        assertThat(updatedSlot.getRemainingCapacity()).isZero();
    }

    /**
     * [통합] 한 달 치 타임슬롯을 일괄 생성하면 실제 DB에 정확한 개수가 저장된다.
     * @throws Exception
     */
    @Test
    @DisplayName("[통합] 한 달 치 타임슬롯을 일괄 생성하면 실제 DB에 정확한 개수가 저장된다.")
    void createTimeSlotsBulk_Integration() {
        // given
        UUID restaurantId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 2);
        LocalTime openTime = LocalTime.of(10, 0);
        LocalTime closeTime = LocalTime.of(12, 0);
        int intervalMinutes = 60;
        int capacity = 4;

        // when
        timeSlotService.createTimeSlotsBulk(
                restaurantId, startDate, endDate, openTime, closeTime, intervalMinutes, capacity);

        // then
        List<TimeSlot> savedSlots = timeSlotRepository.findByDateRange(restaurantId, startDate, endDate);
        assertThat(savedSlots).hasSize(4);
    }
}