package com.michelet.timeslotservice.application.service;

import com.michelet.common.exception.BusinessException;
import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.repository.TimeSlotRepository;
import com.michelet.timeslotservice.support.IntegrationTestSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.michelet.timeslotservice.support.builder.TimeSlotTestBuilder.aTimeSlot;
import static org.assertj.core.api.Assertions.*;

class TimeSlotServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

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


    /**
     * [통합] 1. 정상 흐름: 예약 취소 시 타임슬롯의 남은 자리가 정확히 복구된다.
     */
    @Test
    @DisplayName("[통합] 예약 취소 시 타임슬롯의 남은 자리가 요청한 만큼(1) 정상 복구된다.")
    void restoreCapacity_Success() {
        // given
        TimeSlot slot = aTimeSlot()
            .id(null)
            .restaurantId(UUID.randomUUID())
            .capacity(4)
            .remainingCapacity(2)
            .build();
        TimeSlot savedSlot = timeSlotRepository.save(slot);

        // when
        timeSlotService.restoreCapacity(savedSlot.getId(), 1);

        // then
        TimeSlot updatedSlot = timeSlotRepository.findById(savedSlot.getId()).orElseThrow();
        assertThat(updatedSlot.getRemainingCapacity()).isEqualTo(3);
    }

    /**
     * [통합] 2. 예외 흐름: 정원 초과 방어
     */
    @Test
    @DisplayName("[통합] 최대 수용 인원을 초과하여 복구하려고 하면 BusinessException이 발생한다.")
    void restoreCapacity_Fail_ExceedMaxCapacity() {
        // given
        TimeSlot slot = aTimeSlot()
            .id(null)
            .restaurantId(UUID.randomUUID())
            .capacity(4)
            .remainingCapacity(4)
            .build();
        TimeSlot savedSlot = timeSlotRepository.save(slot);

        // when & then
        assertThatThrownBy(() -> timeSlotService.restoreCapacity(savedSlot.getId(), 1))
                .isInstanceOf(BusinessException.class); 
    }

    /**
     * [통합] 3. 동시성 제어: 다중 취소 요청 시 락(Lock) 검증
     */
    @Test
    @DisplayName("[통합] 동일한 타임슬롯에 4건의 복구 요청이 동시 다발적으로 발생하면 락이 작동해야 한다.")
    void restoreCapacity_ConcurrencyTest() throws InterruptedException {
        // given
        TimeSlot slot = aTimeSlot()
            .id(null)
            .restaurantId(UUID.randomUUID())
            .capacity(4)
            .remainingCapacity(0)
            .build();
        TimeSlot savedSlot = timeSlotRepository.save(slot);

        int threadCount = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    timeSlotService.restoreCapacity(savedSlot.getId(), 1);
                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException | BusinessException e) {
                    failCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Test interrupted", e);
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        start.countDown();
        done.await();
        executorService.shutdown();

        // then
        TimeSlot updatedSlot = timeSlotRepository.findById(savedSlot.getId()).orElseThrow();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(3);
        assertThat(updatedSlot.getRemainingCapacity()).isEqualTo(1);
    }

    
}