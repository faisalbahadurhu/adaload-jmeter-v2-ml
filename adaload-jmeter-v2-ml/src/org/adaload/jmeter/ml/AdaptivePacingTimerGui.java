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
import org.apache.jmeter.timers.gui.AbstractTimerGui;

public class AdaptivePacingTimerGui extends AbstractTimerGui {
    private JTextField initialRate;
    private JTextField minRate;
    private JTextField maxRate;
    private JCheckBox verbose;

    public AdaptivePacingTimerGui() { init(); }
    public String getStaticLabel() { return "AdaLoad Adaptive Pacing Timer"; }
    public String getLabelResource() { return null; }
    public TestElement createTestElement() { AdaptivePacingTimer t = new AdaptivePacingTimer(); modifyTestElement(t); return t; }
    public void modifyTestElement(TestElement e) {
        configureTestElement(e);
        e.setProperty(AdaptivePacingTimer.INITIAL_RATE, clean(initialRate.getText(), "5"));
        e.setProperty(AdaptivePacingTimer.MIN_RATE, clean(minRate.getText(), "1"));
        e.setProperty(AdaptivePacingTimer.MAX_RATE, clean(maxRate.getText(), "100"));
        e.setProperty(AdaptivePacingTimer.VERBOSE, Boolean.toString(verbose.isSelected()));
    }
    public void configure(TestElement e) {
        super.configure(e);
        initialRate.setText(valueOrDefault(e.getPropertyAsString(AdaptivePacingTimer.INITIAL_RATE), "5"));
        minRate.setText(valueOrDefault(e.getPropertyAsString(AdaptivePacingTimer.MIN_RATE), "1"));
        maxRate.setText(valueOrDefault(e.getPropertyAsString(AdaptivePacingTimer.MAX_RATE), "100"));
        verbose.setSelected(Boolean.parseBoolean(valueOrDefault(e.getPropertyAsString(AdaptivePacingTimer.VERBOSE), "false")));
    }
    public void clearGui() {
        super.clearGui(); initialRate.setText("5"); minRate.setText("1"); maxRate.setText("100"); verbose.setSelected(false);
    }
    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("AdaLoad Adaptive Pacing Timer"));
        initialRate = new JTextField("5", 12); minRate = new JTextField("1", 12); maxRate = new JTextField("100", 12); verbose = new JCheckBox("Verbose logging", false);
        addRow(p,0,"Initial planned arrival rate",initialRate);
        addRow(p,1,"Minimum planned arrival rate",minRate);
        addRow(p,2,"Maximum planned arrival rate",maxRate);
        GridBagConstraints c = base(); c.gridy = 3; c.gridx = 1; p.add(verbose, c);
        add(p, BorderLayout.NORTH);
    }
    private static void addRow(JPanel p, int y, String label, JTextField f) { GridBagConstraints c = base(); c.gridx=0; c.gridy=y; c.weightx=0; p.add(new JLabel(label), c); c=base(); c.gridx=1; c.gridy=y; c.weightx=1; c.fill=GridBagConstraints.HORIZONTAL; p.add(f,c); }
    private static GridBagConstraints base(){ GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(4,4,4,4); c.anchor=GridBagConstraints.WEST; return c; }
    private static String clean(String v, String d){ try { return String.valueOf(Double.parseDouble(v.trim())); } catch(Exception e){ return d; } }
    private static String valueOrDefault(String v, String d){ return v == null || v.trim().isEmpty() ? d : v.trim(); }
}
