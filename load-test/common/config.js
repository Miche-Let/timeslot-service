export const BASE_URL = __ENV.BASE_URL || 'http://localhost:19400';
export const TIMESLOT_ID = __ENV.TIMESLOT_ID;
export const INTERNAL_TOKEN = __ENV.INTERNAL_TOKEN;

export function validateEnvironment() {
    if (!INTERNAL_TOKEN || !TIMESLOT_ID) {
        throw new Error('FATAL: 필수 환경 변수 누락 (TIMESLOT_ID, INTERNAL_TOKEN)');
    }
}