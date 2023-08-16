package hr.fer.bernardcrnkovic.mtsp.metric;

import hr.fer.bernardcrnkovic.mtsp.model.DataPoint;
import hr.fer.bernardcrnkovic.mtsp.model.EvolutionState;
import hr.fer.bernardcrnkovic.mtsp.model.Fitness;
import hr.fer.bernardcrnkovic.mtsp.model.Metrics;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

public class FitnessMetricAccumulator implements Consumer<EvolutionState> {
    private Random random = new Random();
    private int index = 0;
    private Fitness lastFitness = null;
    private Queue<Object> queue;
    private String metricName;
    private Metrics metrics;
    private final List<Consumer<DataPoint>> listeners = new ArrayList<>();

    public FitnessMetricAccumulator(String metricName, Metrics metrics) {
        this.metricName = metricName;
        this.metrics = metrics;
    }

    @Override
    public void accept(EvolutionState evolutionState) {
        // append to evolution state
        if (lastFitness != null /* and fitness is worse or equal to lastFitness */) {
            return;
        }
        index += random.nextInt(0, 100);
        var dp = new DataPoint(Instant.now(), Map.of("dummy", index));
        var data = metrics.data.computeIfAbsent(metricName, (key) -> new ArrayList<>());
        data.add(dp);
        listeners.forEach(l -> l.accept(dp));
    }

    public void addListener(Consumer<DataPoint> listener) {
        listeners.add(listener);
    }
}
