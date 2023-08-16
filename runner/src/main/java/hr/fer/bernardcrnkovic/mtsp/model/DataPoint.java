package hr.fer.bernardcrnkovic.mtsp.model;

import java.time.Instant;
import java.util.Map;

public class DataPoint {
    private Instant timestamp;
    private Map<String, Number> metrics;
    public DataPoint(Instant x, Map<String, Number> metrics) {
        this.timestamp = x;
        this.metrics = metrics;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void setMetrics(Map<String, Number> metrics) {
        this.metrics = metrics;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Number> getMetrics() {
        return metrics;
    }
}
