import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 1,
    duration: '10s',
};

const BASE_URL = 'http://localhost:19400/api/v1';
const RESAURANT_ID = __ENV.RESTAURANT_ID;
const YEAR = __ENV.YEAR;
const MONTH = __ENV.MONTH;

export default function () {

    const url = `${BASE_URL}/restaurants/${RESAURANT_ID}/time-slots/calendar?year=${YEAR}&month=${MONTH}`;
    
    const res = http.get(url);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time is < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1); 
}