package hr.fer.bernardcrnkovic.mtsp.algo;

import hr.fer.bernardcrnkovic.mtsp.model.EvolutionConfig;
import hr.fer.bernardcrnkovic.mtsp.model.EvolutionState;
import hr.fer.bernardcrnkovic.mtsp.model.Run;
import hr.fer.bernardcrnkovic.mtsp.model.Solution;
import hr.fer.bernardcrnkovic.mtsp.operator.Crossover;
import hr.fer.bernardcrnkovic.mtsp.operator.Mutation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public class NSGA2 {
    private final EvolutionState state;
    private final EvolutionConfig config;
    private final List<Consumer<EvolutionState>> iterConsumers = new ArrayList<>();
    private final List<Consumer<EvolutionState>> genConsumers = new ArrayList<>();
    private final List<Supplier<Boolean>> stopSignalSuppliers = new ArrayList<>();
    private final Random random = new Random();
    private final List<BiFunction<Solution, Solution, List<Solution>>> recombinators;
    private final List<Function<Solution, Solution>> mutators;

    public NSGA2(Run run) {
        // this.crossoverOperators = new
        this.state = run.state;
        this.config = run.config;
        // TODO: prepare crossover and mutation operators in tight arrays here
        // so we don't waste time with hashtable lookups
        recombinators = new ArrayList<>() {{
            add((s1, s2) -> List.of(Crossover.scx(s1, s2, run.problem)));
            add((s1, s2) -> Crossover.pmx(s1, s2, run.problem, random));
        }};
        mutators = new ArrayList<>() {{
            add(s -> Mutation.singleSwap(s, random));
            add(s -> Mutation.segmentSwap(s, random));
            add(s -> Mutation.invertSwap(s, random));
        }};
    }

    public void run() {
        while (stopSignalSuppliers.stream().noneMatch(Supplier::get)) {
            System.out.println("Iter");
            /* 1. generate N children from population of N parents */
            List<Solution> children = new ArrayList<>();
            while (children.size() < state.population.getSize()) {
                var p1 = FastNonDomSort.select(state.population, 3, random);
                var p2 = FastNonDomSort.select(state.population, 3, random);
                var cs = recombinators.get(random.nextInt(recombinators.size())).apply(p1, p2);
                if (random.nextDouble() < config.getMutationProbability()) {
                    cs.forEach(c -> mutators.get(random.nextInt(mutators.size())).apply(c));
                }
                children.addAll(cs);
            }
            /* 2. combine children & parents into set R of size 2N */
            var mixed = new ArrayList<Solution>(state.population.getSize() + children.size());
            mixed.addAll(state.population.getIndividuals());
            mixed.addAll(children);
            // todo: add cutoff param to stop early when N individuals are sorted
            FastNonDomSort.fastNonDominatedSort(mixed, state.population.getSize());
            state.population.setIndividuals(mixed.subList(0, state.population.getSize()));
            // notify iteration and generation consumers
            iterConsumers.forEach(l -> l.accept(state));
            genConsumers.forEach(l -> l.accept(state));
        }
    }

    /* updated once per iteration */
    public void addIterationConsumer(Consumer<EvolutionState> listener) {
        iterConsumers.add(listener);
    }

    /* updated once per generation */
    public void addGenerationConsumer(Consumer<EvolutionState> listener) {
        genConsumers.add(listener);
    }

    public EvolutionState getState() {
        return state;
    }

    /* */
    public void addStopSignalSupplier(Supplier<Boolean> stopSignalSupplier) {
        stopSignalSuppliers.add(stopSignalSupplier);
    }
}
