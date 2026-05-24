# AdaLoad-JMeter v2 ML Plugin Source Code

This is the full Eclipse-friendly source project for the tested `adaload-jmeter-v2-ml-fix7.jar` plugin.

## JMeter components

The plugin adds two JMeter elements:

1. `AdaLoad Adaptive Pacing Timer`
   - JMeter path: `Thread Group -> Add -> Timer -> AdaLoad Adaptive Pacing Timer`
   - Runtime class: `AdaptivePacingTimer`
   - GUI class: `AdaptivePacingTimerGui`

2. `AdaLoad Runtime Telemetry Listener`
   - JMeter path: `Thread Group -> Add -> Listener -> AdaLoad Runtime Telemetry Listener`
   - Runtime class: `RuntimeTelemetryListener`
   - GUI class: `RuntimeTelemetryListenerGui`

## Main Java classes

- `AdaptivePacingTimer.java`  
  Controls request pacing using the current planned arrival rate.

- `RuntimeTelemetryListener.java`  
  Receives completed JMeter samples, forms telemetry windows, and calls the ML controller.

- `TelemetryWindow.java`  
  Aggregates response time, p95, p99, throughput, and error-rate metrics per monitoring interval.

- `MLRiskController.java`  
  Implements warm-up, ML risk prediction, low-risk increase, warning hold, and high-risk decrease actions.

- `LogisticRiskModel.java`  
  Contains embedded Logistic Regression means, scales, intercept, and coefficients.

- `AdaLoadCsvWriter.java`  
  Writes interval-level ML decision records to CSV.

## Model file

The `model/adaload_logistic_model.json` file contains the trained Logistic Regression coefficients used to create `LogisticRiskModel.java`.

## Eclipse import

1. Open Eclipse.
2. Select `File -> Import -> Existing Projects into Workspace`.
3. Select this folder.
4. Add Apache JMeter libraries to the Build Path:
   - `JMETER_HOME/lib/*.jar`
   - `JMETER_HOME/lib/ext/*.jar`
5. Use JDK 17.

## Build without Maven

Maven is not required. You can export the JAR from Eclipse:

`Right click project -> Export -> Java -> JAR file`

Include:
- compiled classes from `src`
- `META-INF/MANIFEST.MF`
- `META-INF/services/org.apache.jmeter.gui.JMeterGUIComponent`

Place the exported JAR in:

`JMETER_HOME/lib/ext/`

Restart JMeter.

## Tested JAR

The `dist/adaload-jmeter-v2-ml-fix7.jar` file is the tested JAR version used in the latest successful 100 ms, 50 ms, and 30 ms SLO runs.
