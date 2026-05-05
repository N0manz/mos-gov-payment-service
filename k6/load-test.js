import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const fetchErrors  = new Counter('fetch_errors');
const readErrors   = new Counter('read_errors');
const fetchSuccess = new Rate('fetch_success_rate');
const readSuccess  = new Rate('read_success_rate');
const fetchTrend   = new Trend('fetch_duration_ms', true);
const readTrend    = new Trend('read_duration_ms', true);

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
    scenarios: {
        fetch_and_save: {
            executor: 'constant-arrival-rate',
            rate: 200,
            timeUnit: '1s',
            duration: '60s',
            preAllocatedVUs: 300,
            maxVUs: 500,
            exec: 'fetchPayment',
        },
        read_latest: {
            executor: 'constant-arrival-rate',
            rate: 200,
            timeUnit: '1s',
            duration: '60s',
            preAllocatedVUs: 50,
            maxVUs: 200,
            exec: 'readPayments',
        },
    },
    thresholds: {
        'fetch_success_rate':  ['rate>0.95'],
        'read_success_rate':   ['rate>0.99'],
        'fetch_duration_ms':   ['p(95)<2000'],
        'read_duration_ms':    ['p(95)<300'],
        'http_req_failed':     ['rate<0.05'],
    },
};

export function fetchPayment() {
    const start = Date.now();
    const res = http.post(`${BASE_URL}/api/v1/payments/fetch`, null, {
        timeout: '10s',
    });
    fetchTrend.add(Date.now() - start);

    const ok = check(res, {
        'fetch: status 200': (r) => r.status === 200,
        'fetch: has id':     (r) => {
            try { return JSON.parse(r.body).id !== undefined; }
            catch (e) { return false; }        },
    });

    fetchSuccess.add(ok);
    if (!ok) {
        fetchErrors.add(1);
        console.error(`fetch failed [${res.status}]: ${res.body}`);
    }
}

export function readPayments() {
    const start = Date.now();
    const res = http.get(`${BASE_URL}/api/v1/payments?limit=10`, {
        timeout: '5s',
    });
    readTrend.add(Date.now() - start);

    const ok = check(res, {
        'read: status 200':  (r) => r.status === 200,
        'read: is array':    (r) => {
            try { return Array.isArray(JSON.parse(r.body)); }
            catch (e) { return false; }
        },
    });

    readSuccess.add(ok);
    if (!ok) {
        readErrors.add(1);
        console.error(`read failed [${res.status}]: ${res.body}`);
    }
}