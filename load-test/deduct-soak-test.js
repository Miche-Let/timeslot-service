import { sleep } from 'k6';
import { validateEnvironment } from './common/config.js';
import { deduct } from './common/api-client.js';
import { checkResponse } from './common/checks.js';

export const options = {
    stages: [
        { duration: '2m', target: 80 },
        { duration: '4h', target: 80 },
        { duration: '2m', target: 0 },
    ],
};

export function setup() {
    validateEnvironment();
}

export default function () {
    const res = deduct(1);
    
    if (res.status === 500) {
        console.log(`500 응답: ${res.body}`);
    }
    checkResponse(res);
}