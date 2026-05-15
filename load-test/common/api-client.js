import http from 'k6/http';
import { BASE_URL, TIMESLOT_ID, INTERNAL_TOKEN } from './config.js';

const headers = {
    'Content-Type': 'application/json',
    'X-Internal-Token': INTERNAL_TOKEN,
};

export function deduct(requiredCapacity = 1) {
    const url = `${BASE_URL}/internal/v1/time-slots/${TIMESLOT_ID}/deduct`;
    const payload = JSON.stringify({ requiredCapacity });
    return http.patch(url, payload, { headers });
}

export function restore(restoreCapacity = 1) {
    const url = `${BASE_URL}/internal/v1/time-slots/${TIMESLOT_ID}/restore`;
    const payload = JSON.stringify({ restoreCapacity });
    return http.patch(url, payload, { headers });
}