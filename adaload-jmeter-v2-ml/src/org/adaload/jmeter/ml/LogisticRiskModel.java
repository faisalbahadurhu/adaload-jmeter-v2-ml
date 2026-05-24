package org.adaload.jmeter.ml;

final class LogisticRiskModel {
    private static final double[] MEAN = {64.68259712230217,55.5896762589928,20.0,281.7769784172662,22.247521582733814,36.7410071942446,47.539568345323744,0.0,645.2158273381295,5.0,51.740219424460435,2378.4424460431655};
    private static final double[] SCALE = {42.773890745906264,37.113378296009024,1.0,184.96964725397115,10.422648290095498,20.505075044551337,24.86402335438959,1.0,1169.7130205913168,1.0,47.83132386142731,4974.367316907957};
    private static final double INTERCEPT = -2.0497809225217285;
    private static final double[] COEF = {0.24721115738084662,0.27382394301612195,0.0,0.19611533703660516,0.47643406306485747,-0.21014108043383373,-1.0155757627654962,0.0,0.19279500643528422,0.0,2.6051233853038815,1.0174878419149642};

    double predictRisk(double plannedArrivalRate, double observedTps, int activeThreads, int samples,
                       double avgMs, long p95Ms, long p99Ms, double errorRatePercent,
                       long responseSloMs, double errorSloPercent, double sloUsagePercent, long timerDelayMs) {
        double[] x = {plannedArrivalRate, observedTps, activeThreads, samples, avgMs, p95Ms, p99Ms,
                errorRatePercent, responseSloMs, errorSloPercent, sloUsagePercent, timerDelayMs};
        double z = INTERCEPT;
        for (int i = 0; i < x.length; i++) {
            double scaled = (x[i] - MEAN[i]) / (SCALE[i] == 0.0 ? 1.0 : SCALE[i]);
            z += COEF[i] * scaled;
        }
        if (z >= 35.0) return 1.0;
        if (z <= -35.0) return 0.0;
        return 1.0 / (1.0 + Math.exp(-z));
    }
}
