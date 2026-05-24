package org.adaload.jmeter.ml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class TelemetryWindow {
    private final List<Long> responseTimes = new ArrayList<Long>();
    private int errorCount = 0;
    private long windowStartMs = System.currentTimeMillis();

    synchronized void add(long responseTimeMs, boolean success) {
        responseTimes.add(Math.max(0L, responseTimeMs));
        if (!success) errorCount++;
    }

    synchronized WindowStats closeIfDue(long nowMs, long intervalMs, int activeThreads) {
        if (responseTimes.isEmpty() || (nowMs - windowStartMs) < intervalMs) {
            return null;
        }
        WindowStats stats = compute(nowMs, activeThreads);
        responseTimes.clear();
        errorCount = 0;
        windowStartMs = nowMs;
        return stats;
    }

    private WindowStats compute(long nowMs, int activeThreads) {
        WindowStats s = new WindowStats();
        int n = responseTimes.size();
        double sec = Math.max(0.001, (nowMs - windowStartMs) / 1000.0);
        s.windowSeconds = sec;
        s.activeThreads = Math.max(1, activeThreads);
        s.samples = n;
        s.errorCount = errorCount;
        s.errorRatePercent = n > 0 ? (errorCount * 100.0) / n : 0.0;
        s.observedTps = n / sec;
        if (n == 0) return s;
        List<Long> sorted = new ArrayList<Long>(responseTimes);
        Collections.sort(sorted);
        long sum = 0;
        for (Long v : sorted) sum += v.longValue();
        s.avgResponseMs = sum / (double)n;
        s.minResponseMs = sorted.get(0).longValue();
        s.maxResponseMs = sorted.get(n - 1).longValue();
        s.p95ResponseMs = percentile(sorted, 95.0);
        s.p99ResponseMs = percentile(sorted, 99.0);
        return s;
    }

    private static long percentile(List<Long> sorted, double p) {
        if (sorted.isEmpty()) return 0L;
        int index = (int)Math.ceil((p / 100.0) * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        return sorted.get(index).longValue();
    }

    static final class WindowStats {
        double windowSeconds;
        double observedTps;
        int activeThreads;
        int samples;
        double avgResponseMs;
        long p95ResponseMs;
        long p99ResponseMs;
        long minResponseMs;
        long maxResponseMs;
        int errorCount;
        double errorRatePercent;
    }
}
