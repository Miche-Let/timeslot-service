import { sleep } from 'k6';
import { validateEnvironment } from './common/config.js';
import { restore } from './common/api-client.js';
import { checkResponse } from './common/checks.js';

export const options = {
    stages: [
        { duration: '2m', target: 100 },
        { duration: '2m', target: 200 },
        { duration: '2m', target: 300 },
        { duration: '2m', target: 400 },
        { duration: '5m', target: 0 },
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