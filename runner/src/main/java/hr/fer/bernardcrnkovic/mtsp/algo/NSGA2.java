package hr.fer.bernardcrnkovic.mtsp.algo;

import hr.fer.bernardcrnkovic.mtsp.model.EvolutionConfig;
import hr.fer.bernardcrnkovic.mtsp.model.EvolutionState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NSGA2 {
    private final EvolutionState state;
    private final EvolutionConfig config;
    private final List<Consumer<EvolutionState>> iterListeners = new ArrayList<>();
    private final List<Consumer<EvolutionState>> genListeners = new ArrayList<>();
    private final List<Supplier<Boolean>> shouldStopSuppliers = new ArrayList<>();
    private final Random random = new Random();

    public NSGA2(EvolutionState state, EvolutionConfig config) {
        // this.crossoverOperators = new
        this.state = state;
        this.config = config;
        // TODO: prepare crossover and mutation operators in tight arrays here
        // so we don't waste time with hashtable lookups
    }

    public void run() {
        while (shouldStopSuppliers.stream().noneMatch(Supplier::get)) {
            System.out.println("Iter");
            /* 1. generate N children from population of N parents */
            // state
            /* 2. combine children & parents into set R */
            /* 3. */
            try {
                Thread.sleep(random.nextInt(1, 3000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            iterListeners.forEach(l -> l.accept(state));
            genListeners.forEach(l -> l.accept(state));
        }
    }

    /* updated once per iteration */
    public void addIterationListener(Consumer<EvolutionState> listener) {
        iterListeners.add(listener);
    }

    /* updated once per generation */
    public void addGenerationListener(Consumer<EvolutionState> listener) {
        genListeners.add(listener);
    }

    public EvolutionState getState() {
        return state;
    }

    /* */
    public void addStopNotifier(Supplier<Boolean> stopHintSupplier) {
        shouldStopSuppliers.add(stopHintSupplier);
    }
}
