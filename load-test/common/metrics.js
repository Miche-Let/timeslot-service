import { Counter } from 'k6/metrics';

export const lockConflicts = new Counter('lock_conflicts');
export const businessRejects = new Counter('business_rejects'); // 409
export const systemErrors = new Counter('system_errors');        // 500