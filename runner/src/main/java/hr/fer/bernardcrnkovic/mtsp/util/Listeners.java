package hr.fer.bernardcrnkovic.mtsp.util;

import hr.fer.bernardcrnkovic.mtsp.io.Loader;
import hr.fer.bernardcrnkovic.mtsp.metric.MetricExtractors;
import hr.fer.bernardcrnkovic.mtsp.model.EvolutionState;
import hr.fer.bernardcrnkovic.mtsp.model.Run;
import hr.fer.bernardcrnkovic.mtsp.model.Solution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Listeners {
    public static Consumer<EvolutionState> debugger(int frequency) {
        return (state) -> {
            if (!Objects.equals(state.generation % frequency, 0)) return;
            System.out.println("Gen:         " + state.generation);
            System.out.println("Front Sizes: " + state.population.getIndividuals()
                .stream()
                .collect(Collectors.groupingBy(Solution::getRank))
                .values()
                .stream()
                .map(List::size)
                .toList()
            );
            System.out.println("Fittest     :" + state.population.getParettoFront()
                .stream()
                .map(Solution::getFitness)
                .collect(Collectors.groupingBy(f -> f))
                .entrySet()
                .stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().size()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
        };
    }

    public static Consumer<EvolutionState> sleeper(int frequency, int amount) {
        return (state) -> {
            if (Objects.equals(amount, 0)) {
                return;
            }
            try {
                Thread.sleep(amount);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }


    public static Consumer<EvolutionState> stateSnapshotter(int frequency, Run run) {
        return (state) -> {
            if (!Objects.equals(state.generation % frequency, 0)) return;
            var timestamp = Instant.now().toString();
            try {
                Files.writeString(
                    Path.of("output", "%s-bayg-29-%s.json".formatted(
                        "gen" + state.generation,
                        timestamp
                    )),
                    Loader.dump(run)
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static Consumer<EvolutionState> parettoEdgesMetricAdder(int frequency, Run run) {
        return (state) -> {
            if (!Objects.equals(state.generation % 100, 0)) return;
            run.metrics.computeIfAbsent("paretto_edges", (k) -> new ArrayList<>()).add(
                MetricExtractors.extractParettoEdgeFitnesses(state)
            );
        };
    }

}
