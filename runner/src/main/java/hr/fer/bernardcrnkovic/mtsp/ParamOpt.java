package hr.fer.bernardcrnkovic.mtsp;

import hr.fer.bernardcrnkovic.mtsp.algo.NSGA2;
import hr.fer.bernardcrnkovic.mtsp.io.Loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ParamOpt {
    public static void main(String[] args) throws IOException, InterruptedException {
        int repOffset = 0;
        int reps = 50;
        boolean monitorSteadyGens = false;
        int steadyGenStop = 50;
        int maxGens = 100;
        boolean overwrite = false;
        var runNames = List.of(
            "bayg29-run.json"
            // "/eil101-run.json",
            // "/dsj1000-run.json"
        );
        for (var runName : runNames) {
            System.out.println("Run: " + runName);

            /* MUTATION_PROBABILITY optimize */
            for (var mutProb : doubleRange(0.01, 0.5, 0.01)) {
                System.out.println("Mut Prob: " + mutProb);
                /* num of repetitions */
                for (var seed : intRange(0, reps, 1)) {
                    var p = Path.of(
                        "output/param_opt/steady:off/mut_prob/%.2f/rep/%02d".formatted(mutProb, seed),
                        runName
                    );
                    if (!overwrite && p.toFile().exists()) {
                        System.out.printf("Run %s exists. Skipping.%n", p);
                        continue;
                    }
                    // System.out.println("Repetition " + seed);
                    var run = Loader.loadFromResource("/" + runName);
                    run.state.initialSeed = seed + repOffset;
                    run.config.setMonitorSteadyGenerations(monitorSteadyGens);
                    run.config.setStopAfterSteadyGenerations(steadyGenStop);
                    run.config.setMutationProbability((float) mutProb);
                    run.config.setStopAfterGenerations(maxGens);
                    // System.out.println("Loaded label: " + run.label);

                    var nsga = new NSGA2(run);

                    // nsga.addIterationConsumer(Listeners.debugger(1000));
                    // nsga.addIterationConsumer(Listeners.sleeper(1, 500));
                    // nsga.addIterationConsumer(Listeners.parettoEdgesMetricAdder(100, run));

                    nsga.run();
                    System.out.println("GENERS " + run.state.generation);


                    /* persist finished run */
                    Files.createDirectories(p.getParent());
                    Files.writeString(p, Loader.dump(run));
                }
            }
        }
    }

    private static double[] doubleRange(double start, double stop, double step) {
        var ds = new ArrayList<Double>();
        for (double d = start; d < stop; d += step) {
            ds.add(d);
        }
        return ds.stream().mapToDouble(x -> x).toArray();
    }

    private static int[] intRange(int start, int stop, int step) {
        var is = new ArrayList<Integer>();
        for (int i = start; i < stop; i += step) {
            is.add(i);
        }
        return is.stream().mapToInt(x -> x).toArray();
    }
}
