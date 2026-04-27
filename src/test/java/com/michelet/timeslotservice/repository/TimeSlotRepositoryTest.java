package com.michelet.timeslotservice.repository;

import com.michelet.timeslotservice.config.AuditorConfig;
import com.michelet.timeslotservice.domain.TimeSlotStatus;
import com.michelet.timeslotservice.repository.entity.TimeSlotEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuditorConfig.class)
class TimeSlotRepositoryTest {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Test
    @DisplayName("TimeSlotEntity can be saved and found by ID")
    void saveAndFindById_Success() {
        UUID id = UUID.randomUUID();
        TimeSlotEntity entity = TimeSlotEntity.builder()
                .id(id)
                .restaurantId(UUID.randomUUID())
                .targetDate(LocalDate.of(2026, 5, 2))
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(13, 0))
                .capacity(4)
                .remainingCapacity(4)
                .status(TimeSlotStatus.OPENED)
                .build();

        // When
        TimeSlotEntity saved = timeSlotRepository.save(entity);
        TimeSlotEntity found = timeSlotRepository.findById(saved.getId()).get();

        // Then
        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getCreatedAt()).isNotNull();
    }
}