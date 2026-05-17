import { sleep } from 'k6';
import { validateEnvironment } from './common/config.js';
import { restore } from './common/api-client.js';
import { checkResponse } from './common/checks.js';

export const options = {
    vus: 1,
    duration: '10s',
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