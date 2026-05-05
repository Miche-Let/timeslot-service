package com.michelet.timeslotservice.support.builder;

import com.michelet.timeslotservice.domain.TimeSlot;
import com.michelet.timeslotservice.domain.TimeSlotStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * 테스트 데이터 생성을 위한 Test Data Builder.
 * 기본적으로 '예약 가능한 정상 상태(Valid State)'의 데이터를 제공합니다.
 */
public class TimeSlotTestBuilder {

    /**
     * 빌더의 시작점. 기본값으로 초기화된 TimeSlotTestBuilder 인스턴스를 반환합니다.
     */
    public static final UUID DEFAULT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID DEFAULT_RESTAURANT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final LocalDate DEFAULT_DATE = LocalDate.of(2099, 12, 25);
    
    private UUID id = DEFAULT_ID;
    private UUID restaurantId = DEFAULT_RESTAURANT_ID;
    private LocalDate targetDate = DEFAULT_DATE;

    private LocalTime startTime = LocalTime.of(12, 0);
    private LocalTime endTime = LocalTime.of(13, 0);
    
    private int capacity = 4;
    private int remainingCapacity = 4;
    private TimeSlotStatus status = TimeSlotStatus.OPENED;


    /**
     * 빌더의 시작점 메서드. 테스트에서 TimeSlot 객체 생성을 시작할 때 사용합니다.
     * @return
     */
    public static TimeSlotTestBuilder aTimeSlot() {
        return new TimeSlotTestBuilder();
    }

    /**
     * 빌더 메서드들. 각 필드를 설정할 수 있도록 체이닝 방식으로 구현되어 있습니다.
     * @param id
     * @return
     */
    public TimeSlotTestBuilder id(UUID id) { this.id = id; return this; }
    public TimeSlotTestBuilder restaurantId(UUID restaurantId) { this.restaurantId = restaurantId; return this; }
    public TimeSlotTestBuilder targetDate(LocalDate targetDate) { this.targetDate = targetDate; return this; }
    public TimeSlotTestBuilder time(LocalTime start, LocalTime end) { 
        this.startTime = start; 
        this.endTime = end; 
        return this; 
    }
    public TimeSlotTestBuilder capacity(int capacity) { this.capacity = capacity; return this; }
    public TimeSlotTestBuilder remainingCapacity(int remainingCapacity) { this.remainingCapacity = remainingCapacity; return this; }
    public TimeSlotTestBuilder status(TimeSlotStatus status) { this.status = status; return this; }
    public TimeSlotTestBuilder closed() { this.status = TimeSlotStatus.CLOSED; this.remainingCapacity = 0; return this; }


    /**
     * 설정된 필드 값들을 기반으로 TimeSlot 객체를 생성하여 반환합니다.
     * @return
     */
    public TimeSlot build() {
        return TimeSlot.builder()
                .id(id)
                .restaurantId(restaurantId)
                .targetDate(targetDate)
                .startTime(startTime)
                .endTime(endTime)
                .capacity(capacity)
                .remainingCapacity(remainingCapacity)
                .status(status)
                .build();
    }

}