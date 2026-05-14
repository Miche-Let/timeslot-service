import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const lockConflicts = new Counter('lock_conflicts');

export const options = {
    executor: 'ramping-arrival-rate', 
    startRate: 50,
    timeUnit: '1s',
    preAllocatedVUs: 1000,
    maxVUs: 50000,
    stages: [
        { target: 5000, duration: '30m' },
    ],
};

const BASE_URL = 'http://localhost:19400';
const TIMESLOT_ID = __ENV.TIMESLOT_ID;
const INTERNAL_TOKEN = __ENV.INTERNAL_TOKEN;

const TARGET_URL = `${BASE_URL}/internal/v1/time-slots/${TIMESLOT_ID}/restore`;

export default function () {
    if (!INTERNAL_TOKEN || !TIMESLOT_ID) {
        console.error("FATAL ERROR: 필수 환경 변수 누락 (TIMESLOT_ID 또는 INTERNAL_TOKEN)");
        return;
    }

    const payload = JSON.stringify({
        restoreCapacity: 1 
    });
    
    const params = {
        headers: { 
            'Content-Type': 'application/json',
            'X-Internal-Token': INTERNAL_TOKEN
        },
    };

    const res = http.patch(TARGET_URL, payload, params);

    check(res, {
        'is status 200 (예약 성공)': (r) => r.status === 200,
        'is lock conflict (락 충돌 실패)': (r) => r.status === 500 || r.status === 400,
    });

    if (res.status === 500 || res.status === 400) { 
        lockConflicts.add(1);
    }

    sleep(0.1); 
}