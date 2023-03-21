package hr.fer.bernardcrnkovic.mtsp.algo;

import hr.fer.bernardcrnkovic.mtsp.model.EvolutionState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NSGA2 {
    private final EvolutionState state;
    private final List<Consumer<EvolutionState>> iterListeners = new ArrayList<>();
    private final List<Consumer<EvolutionState>> genListeners = new ArrayList<>();
    private final List<Supplier<Boolean>> shouldStopSuppliers = new ArrayList<>();

    public NSGA2(EvolutionState state) {
        this.state = state;
    }

    /* updated once per iteration */
    public void addIterationListener(Consumer<EvolutionState> listener) {
        iterListeners.add(listener);
    }

    /* updated once per generation */
    public void addGenerationListener(Consumer<EvolutionState> listener) {
        iterListeners.add(listener);
    }

    public EvolutionState getState() {
        return state;
    }

    /* */
    public void addStopNotifier(Supplier<Boolean> stopHintSupplier) {
        shouldStopSuppliers.add(stopHintSupplier);
    }

    public void run() {
        while (shouldStopSuppliers.stream().noneMatch(Supplier::get)) {
            /* 1. generate N children from population of N parents */
            // state
            /* 2. combine children & parents into set R */
            /* 3. */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            iterListeners.forEach(l -> l.accept(state));
        }
    }
}
