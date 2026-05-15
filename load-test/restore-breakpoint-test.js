import { sleep } from 'k6';
import { validateEnvironment } from './common/config.js';
import { restore } from './common/api-client.js';
import { checkResponse } from './common/checks.js';

export const options = {
    executor: 'ramping-arrival-rate', 
    startRate: 50,
    timeUnit: '1s',
    preAllocatedVUs: 500,
    maxVUs: 2000,
    stages: [
        { target: 1000, duration: '5m' },
        { target: 1000, duration: '10m' },
        { target: 0, duration: '2m' },
    ],
};

export function setup() {
    validateEnvironment();
}

export default function () {
    const res = restore(1);
    
    if (res.status === 500) {
        console.log(`500 응답: ${res.body}`);
    }
    checkResponse(res);
}