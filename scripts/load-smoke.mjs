import { performance } from "node:perf_hooks";

const baseUrl = process.env.API_BASE_URL ?? "http://127.0.0.1:8080";
const durationMs = Number(process.env.LOAD_DURATION_MS ?? 10_000);
const concurrency = Number(process.env.LOAD_CONCURRENCY ?? 8);
const p95LimitMs = Number(process.env.LOAD_P95_LIMIT_MS ?? 500);
const deadline = performance.now() + durationMs;
const samples = [];
let failures = 0;

async function worker(workerId) {
  while (performance.now() < deadline) {
    const started = performance.now();
    try {
      const response = await fetch(`${baseUrl}/api/v1/system/status`, {
        headers: { "X-Request-ID": `load-${workerId}-${samples.length}` },
        signal: AbortSignal.timeout(2_000),
      });
      if (!response.ok) failures += 1;
      await response.arrayBuffer();
    } catch {
      failures += 1;
    } finally {
      samples.push(performance.now() - started);
    }
  }
}

await Promise.all(Array.from({ length: concurrency }, (_, index) => worker(index)));
samples.sort((left, right) => left - right);
const p95 = samples[Math.max(0, Math.ceil(samples.length * 0.95) - 1)] ?? Infinity;
const errorRate = samples.length === 0 ? 1 : failures / samples.length;
const result = {
  requests: samples.length,
  concurrency,
  durationMs,
  p95Ms: Number(p95.toFixed(2)),
  errorRate: Number(errorRate.toFixed(6)),
  thresholdMs: p95LimitMs,
};
console.log(JSON.stringify(result));
if (samples.length < concurrency || failures > 0 || p95 > p95LimitMs) process.exitCode = 1;
