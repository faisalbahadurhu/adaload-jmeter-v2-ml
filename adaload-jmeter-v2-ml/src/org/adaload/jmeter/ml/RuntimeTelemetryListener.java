package org.adaload.jmeter.ml;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;

public class RuntimeTelemetryListener extends AbstractTestElement implements SampleListener, TestStateListener, NoThreadClone {
    public static final String TELEMETRY_INTERVAL_SECONDS = "AdaLoadTelemetry.intervalSeconds";
    public static final String WARMUP_SECONDS = "AdaLoadTelemetry.warmupSeconds";
    public static final String RESPONSE_SLO_MS = "AdaLoadTelemetry.responseSloMs";
    public static final String ERROR_SLO_PERCENT = "AdaLoadTelemetry.errorSloPercent";
    public static final String INCREASE_PERCENT = "AdaLoadTelemetry.increasePercent";
    public static final String DECREASE_PERCENT = "AdaLoadTelemetry.decreasePercent";
    public static final String RISK_THRESHOLD = "AdaLoadTelemetry.riskThreshold";
    public static final String HOLD_RISK_THRESHOLD = "AdaLoadTelemetry.holdRiskThreshold";
    public static final String HOLD_SLO_USAGE_PERCENT = "AdaLoadTelemetry.holdSloUsagePercent";
    public static final String WRITE_CSV = "AdaLoadTelemetry.writeCsv";
    public static final String CSV_PATH = "AdaLoadTelemetry.csvPath";
    public static final String VERBOSE = "AdaLoadTelemetry.verbose";
    private transient TelemetryWindow window;

    public void testStarted() { configureController(); window = new TelemetryWindow(); MLRiskController.getInstance().testStarted(); }
    public void testStarted(String host) { testStarted(); }
    public void testEnded() { MLRiskController.getInstance().testEnded(); window = null; }
    public void testEnded(String host) { testEnded(); }
    public void sampleOccurred(SampleEvent event) {
        if (event == null || event.getResult() == null) return;
        if (window == null) window = new TelemetryWindow();
        SampleResult r = event.getResult();
        window.add(r.getTime(), r.isSuccessful());
        long now = System.currentTimeMillis();
        long intervalMs = MLRiskController.getInstance().getTelemetryIntervalSeconds() * 1000L;
        int threads = Math.max(1, JMeterContextService.getNumberOfThreads());
        TelemetryWindow.WindowStats stats = window.closeIfDue(now, intervalMs, threads);
        if (stats != null) {
            MLRiskController.getInstance().decide(stats);
        }
    }
    public void sampleStarted(SampleEvent event) { }
    public void sampleStopped(SampleEvent event) { }

    private void configureController() {
        MLRiskController.getInstance().configureListener(parseInt(TELEMETRY_INTERVAL_SECONDS,5), parseInt(WARMUP_SECONDS,15),
            parseLong(RESPONSE_SLO_MS,100L), parseDouble(ERROR_SLO_PERCENT,5.0), parseDouble(INCREASE_PERCENT,20.0),
            parseDouble(DECREASE_PERCENT,30.0), parseDouble(RISK_THRESHOLD,0.50), parseDouble(HOLD_RISK_THRESHOLD,0.35),
            parseDouble(HOLD_SLO_USAGE_PERCENT,70.0), parseBoolean(WRITE_CSV,true), getPropertyAsString(CSV_PATH,"adaload-ml-decisions.csv"), parseBoolean(VERBOSE,false));
    }
    private int parseInt(String k,int d){ try{return Integer.parseInt(getPropertyAsString(k,String.valueOf(d)));}catch(Exception e){return d;} }
    private long parseLong(String k,long d){ try{return Long.parseLong(getPropertyAsString(k,String.valueOf(d)));}catch(Exception e){return d;} }
    private double parseDouble(String k,double d){ try{return Double.parseDouble(getPropertyAsString(k,String.valueOf(d)));}catch(Exception e){return d;} }
    private boolean parseBoolean(String k,boolean d){ try{return Boolean.parseBoolean(getPropertyAsString(k,String.valueOf(d)));}catch(Exception e){return d;} }
}
