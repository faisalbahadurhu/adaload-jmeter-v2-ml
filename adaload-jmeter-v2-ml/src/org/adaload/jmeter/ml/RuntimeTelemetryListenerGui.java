package org.adaload.jmeter.ml;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;

public class RuntimeTelemetryListenerGui extends AbstractListenerGui {
    private JTextField interval, warmup, responseSlo, errorSlo, increase, decrease, riskThreshold, holdRiskThreshold, holdSloUsage, csvPath;
    private JCheckBox writeCsv, verbose;

    public RuntimeTelemetryListenerGui() { init(); }
    public String getStaticLabel() { return "AdaLoad Runtime Telemetry Listener"; }
    public String getLabelResource() { return null; }
    public TestElement createTestElement() { RuntimeTelemetryListener l = new RuntimeTelemetryListener(); modifyTestElement(l); return l; }
    public void modifyTestElement(TestElement e) {
        configureTestElement(e);
        e.setProperty(RuntimeTelemetryListener.TELEMETRY_INTERVAL_SECONDS, cleanInt(interval.getText(), "5"));
        e.setProperty(RuntimeTelemetryListener.WARMUP_SECONDS, cleanInt(warmup.getText(), "15"));
        e.setProperty(RuntimeTelemetryListener.RESPONSE_SLO_MS, cleanLong(responseSlo.getText(), "100"));
        e.setProperty(RuntimeTelemetryListener.ERROR_SLO_PERCENT, cleanDouble(errorSlo.getText(), "5"));
        e.setProperty(RuntimeTelemetryListener.INCREASE_PERCENT, cleanDouble(increase.getText(), "20"));
        e.setProperty(RuntimeTelemetryListener.DECREASE_PERCENT, cleanDouble(decrease.getText(), "30"));
        e.setProperty(RuntimeTelemetryListener.RISK_THRESHOLD, cleanDouble(riskThreshold.getText(), "0.50"));
        e.setProperty(RuntimeTelemetryListener.HOLD_RISK_THRESHOLD, cleanDouble(holdRiskThreshold.getText(), "0.35"));
        e.setProperty(RuntimeTelemetryListener.HOLD_SLO_USAGE_PERCENT, cleanDouble(holdSloUsage.getText(), "70"));
        e.setProperty(RuntimeTelemetryListener.WRITE_CSV, Boolean.toString(writeCsv.isSelected()));
        e.setProperty(RuntimeTelemetryListener.CSV_PATH, csvPath.getText().trim());
        e.setProperty(RuntimeTelemetryListener.VERBOSE, Boolean.toString(verbose.isSelected()));
    }
    public void configure(TestElement e) {
        super.configure(e);
        interval.setText(valueOrDefault(e.getPropertyAsString(RuntimeTelemetryListener.TELEMETRY_INTERVAL_SECONDS), "5"));
        warmup.setText(valueOrDefault(e.getPropertyAsString(RuntimeTelemetryListener.WARMUP_SECONDS), "15"));
        responseSlo.setText(valueOrDefault(e.getPropertyAsString(RuntimeTelemetryListener.RESPONSE_SLO_MS), "100"));
        errorSlo.setText(valueOrDefault(e.getPropertyAsString(RuntimeTelemetryListener.ERROR_SLO_PERCENT), "5"));
        increase.setText(valueOrDefault(e.getPropertyAsString(RuntimeTelemetryListener.INCREASE_PERCENT), "20"));
        decrease.setText(valueOrDefault(e.getPropertyAsString(RuntimeTelemetryListener.DECREASE_PERCENT), "30"));
        riskThreshold.setText(valueOrDefault(e.getPropertyAsString(RuntimeTelemetryListener.RISK_THRESHOLD), "0.50"));
        holdRiskThreshold.setText(valueOrDefault(e.getPropertyAsString(RuntimeTelemetryListener.HOLD_RISK_THRESHOLD), "0.35"));
        holdSloUsage.setText(valueOrDefault(e.getPropertyAsString(RuntimeTelemetryListener.HOLD_SLO_USAGE_PERCENT), "70"));
        writeCsv.setSelected(Boolean.parseBoolean(valueOrDefault(e.getPropertyAsString(RuntimeTelemetryListener.WRITE_CSV), "true")));
        csvPath.setText(valueOrDefault(e.getPropertyAsString(RuntimeTelemetryListener.CSV_PATH), "adaload-ml-decisions.csv"));
        verbose.setSelected(Boolean.parseBoolean(valueOrDefault(e.getPropertyAsString(RuntimeTelemetryListener.VERBOSE), "false")));
    }
    public void clearGui() {
        super.clearGui(); interval.setText("5"); warmup.setText("15"); responseSlo.setText("100"); errorSlo.setText("5"); increase.setText("20"); decrease.setText("30"); riskThreshold.setText("0.50"); holdRiskThreshold.setText("0.35"); holdSloUsage.setText("70"); writeCsv.setSelected(true); csvPath.setText("adaload-ml-decisions.csv"); verbose.setSelected(false);
    }
    private void init() {
        setLayout(new BorderLayout(0,5)); setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JPanel p = new JPanel(new GridBagLayout()); p.setBorder(BorderFactory.createTitledBorder("AdaLoad Runtime Telemetry Listener"));
        interval = new JTextField("5",12); warmup = new JTextField("15",12); responseSlo = new JTextField("100",12); errorSlo = new JTextField("5",12);
        increase = new JTextField("20",12); decrease = new JTextField("30",12); riskThreshold = new JTextField("0.50",12); holdRiskThreshold = new JTextField("0.35",12); holdSloUsage = new JTextField("70",12);
        writeCsv = new JCheckBox("Write ML decision CSV", true); csvPath = new JTextField("adaload-ml-decisions.csv",20); verbose = new JCheckBox("Verbose logging", false);
        addRow(p,0,"Telemetry interval seconds",interval); addRow(p,1,"Warm-up seconds",warmup); addRow(p,2,"p95 response-time SLO ms",responseSlo); addRow(p,3,"Error-rate SLO percent",errorSlo);
        addRow(p,4,"Increase percent",increase); addRow(p,5,"Decrease percent",decrease); addRow(p,6,"ML risk threshold",riskThreshold); addRow(p,7,"HOLD risk threshold",holdRiskThreshold); addRow(p,8,"HOLD SLO usage percent",holdSloUsage);
        GridBagConstraints c = base(); c.gridx=1; c.gridy=9; p.add(writeCsv,c); addRow(p,10,"ML decision CSV path",csvPath); c=base(); c.gridx=1; c.gridy=11; p.add(verbose,c);
        add(p, BorderLayout.NORTH);
    }
    private static void addRow(JPanel p, int y, String label, JTextField f) { GridBagConstraints c = base(); c.gridx=0; c.gridy=y; c.weightx=0; p.add(new JLabel(label), c); c=base(); c.gridx=1; c.gridy=y; c.weightx=1; c.fill=GridBagConstraints.HORIZONTAL; p.add(f,c); }
    private static GridBagConstraints base(){ GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(4,4,4,4); c.anchor=GridBagConstraints.WEST; return c; }
    private static String cleanDouble(String v, String d){ try { return String.valueOf(Double.parseDouble(v.trim())); } catch(Exception e){ return d; } }
    private static String cleanInt(String v, String d){ try { return String.valueOf(Integer.parseInt(v.trim())); } catch(Exception e){ return d; } }
    private static String cleanLong(String v, String d){ try { return String.valueOf(Long.parseLong(v.trim())); } catch(Exception e){ return d; } }
    private static String valueOrDefault(String v, String d){ return v == null || v.trim().isEmpty() ? d : v.trim(); }
}
