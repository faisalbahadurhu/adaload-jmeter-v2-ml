package org.adaload.jmeter.ml;

import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MLRiskController {
    private static final Logger LOG = LoggerFactory.getLogger(MLRiskController.class);
    private static final MLRiskController INSTANCE = new MLRiskController();

    private final LogisticRiskModel model = new LogisticRiskModel();
    private double plannedArrivalRate = 5.0;
    private double minRate = 1.0;
    private double maxRate = 100.0;
    private int intervalSeconds = 5;
    private int warmupSeconds = 15;
    private long responseSloMs = 100L;
    private double errorSloPercent = 5.0;
    private double increasePercent = 20.0;
    private double decreasePercent = 30.0;
    private double riskThreshold = 0.50;
    private double holdRiskThreshold = 0.35;
    private double holdSloUsagePercent = 70.0;
    private boolean writeCsv = true;
    private boolean verbose = false;
    private String csvPath = "adaload-ml-decisions.csv";
    private long timerDelayMs = 0L;
    private long testStartMs = 0L;
    private AdaLoadCsvWriter writer;

    static MLRiskController getInstance() { return INSTANCE; }

    synchronized void configureTimer(double initial, double min, double max, boolean verbose) {
        this.minRate = min > 0 ? min : 1.0;
        this.maxRate = max > 0 ? max : 100.0;
        if (this.minRate > this.maxRate) { double t = this.minRate; this.minRate = this.maxRate; this.maxRate = t; }
        this.plannedArrivalRate = clamp(initial > 0 ? initial : 5.0, this.minRate, this.maxRate);
        this.verbose = this.verbose || verbose;
    }

    synchronized void configureListener(int interval, int warmup, long responseSlo, double errorSlo,
            double increase, double decrease, double riskThreshold, double holdRiskThreshold,
            double holdSloUsagePercent, boolean writeCsv, String csvPath, boolean verbose) {
        this.intervalSeconds = Math.max(1, interval);
        this.warmupSeconds = Math.max(0, warmup);
        this.responseSloMs = Math.max(1L, responseSlo);
        this.errorSloPercent = Math.max(0.0001, errorSlo);
        this.increasePercent = Math.max(0.0, increase);
        this.decreasePercent = Math.max(0.0, decrease);
        this.riskThreshold = clamp(riskThreshold, 0.0, 1.0);
        this.holdRiskThreshold = clamp(holdRiskThreshold, 0.0, 1.0);
        this.holdSloUsagePercent = clamp(holdSloUsagePercent, 0.0, 100.0);
        this.writeCsv = writeCsv;
        this.csvPath = (csvPath == null || csvPath.trim().isEmpty()) ? "adaload-ml-decisions.csv" : csvPath.trim();
        this.verbose = this.verbose || verbose;
    }

    synchronized void testStarted() {
        testStartMs = System.currentTimeMillis();
        if (writeCsv) {
            writer = new AdaLoadCsvWriter(csvPath);
            writer.open();
        }
        LOG.info("[AdaLoad v2 ML fix7] started csv={}", csvPath);
    }

    synchronized void testEnded() {
        if (writer != null) { writer.close(); writer = null; }
        LOG.info("[AdaLoad v2 ML fix7] stopped");
    }

    synchronized Decision decide(TelemetryWindow.WindowStats s) {
        long now = System.currentTimeMillis();
        double elapsed = testStartMs > 0 ? (now - testStartMs) / 1000.0 : 0.0;
        double old = plannedArrivalRate;
        double responseUsage = (s.p95ResponseMs * 100.0) / responseSloMs;
        double errorUsage = (s.errorRatePercent * 100.0) / errorSloPercent;
        double sloUsage = Math.max(responseUsage, errorUsage);
        boolean currentViolation = (s.p95ResponseMs > responseSloMs) || (s.errorRatePercent > errorSloPercent);

        double risk = model.predictRisk(plannedArrivalRate, s.observedTps, s.activeThreads, s.samples,
                s.avgResponseMs, s.p95ResponseMs, s.p99ResponseMs, s.errorRatePercent,
                responseSloMs, errorSloPercent, sloUsage, timerDelayMs);
        int predicted = risk >= riskThreshold ? 1 : 0;
        String state;
        String action;
        if (elapsed < warmupSeconds) {
            state = "WARMUP"; action = "WARMUP";
        } else if (currentViolation || predicted == 1) {
            state = currentViolation ? "CURRENT_VIOLATION" : "PREDICTED_RISK";
            action = "DECREASE";
            plannedArrivalRate = Math.max(minRate, old * (1.0 - decreasePercent / 100.0));
        } else if (risk >= holdRiskThreshold || sloUsage >= holdSloUsagePercent) {
            state = "RISK_WARNING"; action = "HOLD";
        } else {
            state = "LOW_RISK"; action = "INCREASE";
            plannedArrivalRate = Math.min(maxRate, old * (1.0 + increasePercent / 100.0));
        }
        plannedArrivalRate = clamp(plannedArrivalRate, minRate, maxRate);

        Decision d = new Decision();
        d.elapsedSeconds = elapsed; d.epochMs = now; d.windowSeconds = s.windowSeconds;
        d.oldRate = old; d.newRate = plannedArrivalRate; d.plannedRate = plannedArrivalRate;
        d.observedTps = s.observedTps; d.activeThreads = s.activeThreads; d.samples = s.samples;
        d.avgMs = s.avgResponseMs; d.p95Ms = s.p95ResponseMs; d.p99Ms = s.p99ResponseMs;
        d.minMs = s.minResponseMs; d.maxMs = s.maxResponseMs; d.errorCount = s.errorCount;
        d.errorRate = s.errorRatePercent; d.responseSloMs = responseSloMs; d.errorSlo = errorSloPercent;
        d.sloUsage = sloUsage; d.overloadRisk = risk; d.predictedLabel = predicted;
        d.currentViolation = currentViolation ? 1 : 0; d.controlState = state; d.action = action; d.timerDelayMs = timerDelayMs;
        if (writer != null) writer.write(d);
        if (verbose) LOG.info("[AdaLoad v2 ML fix7] {}", d.toCsvLine());
        return d;
    }

    synchronized double getPlannedArrivalRate() { return plannedArrivalRate; }
    synchronized int getTelemetryIntervalSeconds() { return intervalSeconds; }
    synchronized void setTimerDelayMs(long ms) { timerDelayMs = Math.max(0L, ms); }

    private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }

    static final class Decision {
        double elapsedSeconds; long epochMs; double windowSeconds; double oldRate; double newRate; double plannedRate;
        double observedTps; int activeThreads; int samples; double avgMs; long p95Ms; long p99Ms; long minMs; long maxMs;
        int errorCount; double errorRate; long responseSloMs; double errorSlo; double sloUsage; double overloadRisk;
        int predictedLabel; int currentViolation; String controlState; String action; long timerDelayMs;
        String toCsvLine() {
            return String.format(Locale.US,
                "%.3f,%d,%.3f,%.3f,%.3f,%.3f,%.3f,%d,%d,%.3f,%d,%d,%d,%d,%d,%.3f,%d,%.3f,%.3f,%.6f,%d,%d,%s,%s,%d",
                elapsedSeconds, epochMs, windowSeconds, oldRate, newRate, plannedRate, observedTps, activeThreads, samples,
                avgMs, p95Ms, p99Ms, minMs, maxMs, errorCount, errorRate, responseSloMs, errorSlo, sloUsage,
                overloadRisk, predictedLabel, currentViolation, controlState, action, timerDelayMs);
        }
    }
}
