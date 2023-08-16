package hr.fer.bernardcrnkovic.mtsp;

import hr.fer.bernardcrnkovic.mtsp.algo.NSGA2;
import hr.fer.bernardcrnkovic.mtsp.io.Loader;
import hr.fer.bernardcrnkovic.mtsp.util.Listeners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;


public class Parsing2 {
    public static void main(String[] args) throws IOException, InterruptedException {
        Files.createDirectories(Path.of("output"));
        var run = Loader.loadFromResource("/bayg29-run.json");
        run.metrics.computeIfAbsent("paretto_edges", (k) -> new ArrayList<>());

        var nsga = new NSGA2(run);
        nsga.addStopSignalSupplier(() -> false);

        nsga.addIterationConsumer(Listeners.debugger(100));
        nsga.addIterationConsumer(Listeners.sleeper(1, 500));
        nsga.addIterationConsumer(Listeners.parettoEdgesMetricAdder(100, run));
        nsga.addIterationConsumer(Listeners.stateSnapshotter(5000, run));

        nsga.run();

        /* persist */
        var timestamp = Instant.now().toString();
        Files.writeString(
            Path.of("output", "bayg-29-%s.json".formatted(timestamp)),
            Loader.dump(run)
        );
    }
}
