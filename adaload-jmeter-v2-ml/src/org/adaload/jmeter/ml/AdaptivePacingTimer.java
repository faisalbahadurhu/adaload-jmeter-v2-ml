package org.adaload.jmeter.ml;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.timers.Timer;

public class AdaptivePacingTimer extends AbstractTestElement implements Timer, TestStateListener, NoThreadClone {
    public static final String INITIAL_RATE = "AdaLoadPacingTimer.initialRate";
    public static final String MIN_RATE = "AdaLoadPacingTimer.minRate";
    public static final String MAX_RATE = "AdaLoadPacingTimer.maxRate";
    public static final String VERBOSE = "AdaLoadPacingTimer.verbose";

    public long delay() {
        MLRiskController c = MLRiskController.getInstance();
        int activeThreads = Math.max(1, JMeterContextService.getNumberOfThreads());
        double rate = Math.max(0.001, c.getPlannedArrivalRate());
        long delayMs = Math.max(0L, Math.round((activeThreads * 1000.0) / rate));
        c.setTimerDelayMs(delayMs);
        return delayMs;
    }

    public void testStarted() { configureController(); }
    public void testStarted(String host) { testStarted(); }
    public void testEnded() { }
    public void testEnded(String host) { testEnded(); }

    private void configureController() {
        MLRiskController.getInstance().configureTimer(
            parseDouble(INITIAL_RATE, 5.0), parseDouble(MIN_RATE, 1.0), parseDouble(MAX_RATE, 100.0), parseBoolean(VERBOSE, false));
    }
    private double parseDouble(String key, double def) { try { return Double.parseDouble(getPropertyAsString(key, String.valueOf(def))); } catch(Exception e){ return def; } }
    private boolean parseBoolean(String key, boolean def) { try { return Boolean.parseBoolean(getPropertyAsString(key, String.valueOf(def))); } catch(Exception e){ return def; } }
}
