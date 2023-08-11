package hr.fer.bernardcrnkovic.mtsp.algo;

import hr.fer.bernardcrnkovic.mtsp.model.*;
import hr.fer.bernardcrnkovic.mtsp.operator.Crossover;
import hr.fer.bernardcrnkovic.mtsp.operator.EncDec;
import hr.fer.bernardcrnkovic.mtsp.operator.FitnessUtils;
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
    private final Random random = new Random(4);
    private final BiFunction<Solution, Solution, List<Solution>> recombinator;
    private final Function<Solution, Solution> mutator;
    private final Problem problem;

    public NSGA2(Run run) {
        this.state = run.state;
        this.config = run.config;
        this.problem = run.problem;

        var cxOpNames = run.config.getCrossoverOperators();
        var cxOps = new ArrayList<BiFunction<Solution, Solution, List<Solution>>>() {{
            if (cxOpNames.contains("scx")) {
                add((s1, s2) -> List.of(Crossover.scx(s1, s2, run.problem)));
            }
            if (cxOpNames.contains("pmx")) {
                add((s1, s2) -> Crossover.pmx(s1, s2, run.problem, random));
            }
//            if (cxOpNames.contains("aex")) {
//                add((s1, s2) -> List.of(Crossover.aex(s1, s2, run.problem)));
//            }
        }};
        var mutOps = new ArrayList<Function<Solution, Solution>>() {{
            var mutOps = run.config.getMutationOperators();
            if (mutOps.contains("swap")) {
                add(s -> Mutation.singleSwap(s, random));
            }
            if (mutOps.contains("swap-segment")) {
                add(s -> Mutation.segmentSwap(s, random));
            }
            if (mutOps.contains("invert-segment")) {
                add(s -> Mutation.invertSwap(s, random));
            }
        }};

        /* Set up meta-recombinator and meta-mutator */
        recombinator = (s1, s2) -> cxOps.get(random.nextInt(cxOps.size())).apply(s1, s2);
        mutator = (s1) -> mutOps.get(random.nextInt(mutOps.size())).apply(s1);
    }

    public void run() {
        while (stopSignalSuppliers.stream().noneMatch(Supplier::get)) {
            state.iteration += 1;
            System.out.println("Iter " + state.iteration);
            // compute fitnesses where necessary
            state.population.getIndividuals().forEach(s -> {
                if (!s.isEvaluated()) {
                    FitnessUtils.computeFitness(s, problem);
                }
            });
            /* 1. generate N children from population of N parents */
            List<Solution> children = new ArrayList<>();
            while (children.size() < state.population.getSize()) {
                var p1 = FastNonDomSort.select(state.population, 3, random);
                var p2 = FastNonDomSort.select(state.population, 3, random);
                var cs = recombinator.apply(p1, p2);
                cs.forEach(s -> FitnessUtils.computeFitness(s, problem));
                if (random.nextDouble() < config.getMutationProbability()) {
                    cs.forEach(mutator::apply);
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
