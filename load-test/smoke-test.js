import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 1,
    duration: '10s',
};

const BASE_URL = 'http://localhost:19400/api/v1';

const RESTAURANT_ID = 'b2eb25ef-e702-41a3-88fc-690b9983c901'; 

export default function () {

    const url = `${BASE_URL}/restaurants/${RESTAURANT_ID}/time-slots/calendar?year=2026&month=5`;
    
    const res = http.get(url);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time is < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1); 
}