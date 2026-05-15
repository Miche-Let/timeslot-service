import { check } from 'k6';
import { lockConflicts } from './metrics.js';
import { businessRejects } from './metrics.js';
import { systemErrors } from './metrics.js';

export function checkResponse(res) {
    check(res, {
        'expected response (200/409/500)': (r) =>
            r.status === 200 || r.status === 409 || r.status === 500,
    });

    if (res.status === 500 || res.status === 409) lockConflicts.add(1);

    if (res.status === 409) businessRejects.add(1);
    if (res.status === 500) systemErrors.add(1);
}