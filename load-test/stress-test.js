import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 100 },
        { duration: '20s', target: 500 },
        { duration: '30s', target: 500 },
        { duration: '10s', target: 0 },
    ],
};

const TARGET_URL = 'http://localhost:19400/internal/v1/timeslots/a64c62c9-2e99-4790-8fe5-a84921c2a7d3/deduct';
const INTERNAL_TOKEN = __ENV.INTERNAL_TOKEN;

export default function () {
    if (!INTERNAL_TOKEN) {
        console.error("FATAL ERROR: X-Internal-Token 환경 변수가 누락되었습니다.");
        return;
    }

    const payload = JSON.stringify({ requiredCapacity: 1 });
    const params = {
        headers: { 
            'Content-Type': 'application/json',
            'X-Internal-Token': INTERNAL_TOKEN
        },
    };

    const res = http.post(TARGET_URL, payload, params);

    check(res, {
        'is status 200 (Success)': (r) => r.status === 200,
        'is status 400 or 500 (Capacity Full / Lock Conflict)': (r) => r.status === 400 || r.status === 500,
    });

    sleep(0.1); 
}