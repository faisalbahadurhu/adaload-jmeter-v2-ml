package org.adaload.jmeter.ml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

final class AdaLoadCsvWriter {
    private final String csvPath;
    private BufferedWriter writer;

    AdaLoadCsvWriter(String csvPath) { this.csvPath = csvPath; }

    synchronized void open() {
        try {
            Path path = Paths.get(csvPath);
            Path parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
            writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            writer.write("elapsed_s,epoch_ms,window_seconds,old_planned_arrival_rate,new_planned_arrival_rate,planned_arrival_rate,observed_tps,active_threads,samples,avg_response_ms,p95_response_ms,p99_response_ms,min_response_ms,max_response_ms,error_count,error_rate_percent,response_slo_ms,error_slo_percent,slo_usage_percent,overload_risk,predicted_label,current_slo_violation,control_state,action,timer_delay_ms");
            writer.newLine();
            writer.flush();
        } catch (IOException ex) { writer = null; }
    }

    synchronized void write(MLRiskController.Decision d) {
        if (writer == null || d == null) return;
        try { writer.write(d.toCsvLine()); writer.newLine(); writer.flush(); } catch (IOException ex) { }
    }

    synchronized void close() {
        if (writer == null) return;
        try { writer.flush(); writer.close(); } catch (IOException ex) { } finally { writer = null; }
    }
}
