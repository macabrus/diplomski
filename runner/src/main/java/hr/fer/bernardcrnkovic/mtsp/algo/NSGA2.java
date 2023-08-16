package hr.fer.bernardcrnkovic.mtsp.algo;

import hr.fer.bernardcrnkovic.mtsp.model.*;
import hr.fer.bernardcrnkovic.mtsp.operator.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public class NSGA2 {
    private final EvolutionState state;
    private final EvolutionConfig config;
    private final List<Consumer<EvolutionState>> iterConsumers = new ArrayList<>();
    private final List<Supplier<Boolean>> stopSignalSuppliers = new ArrayList<>();
    private final Random random;
    private final BiFunction<Solution, Solution, List<Solution>> recombinator;
    private final Function<Solution, Solution> mutator;
    private final Problem problem;

    public NSGA2(Run run) {
        this.random = new Random(run.state.initialSeed);
        this.state = run.state;
        this.config = run.config;
        this.problem = run.problem;

        var cxOpNames = run.config.getCrossoverOperators();
        var cxOps = new ArrayList<BiFunction<Solution, Solution, List<Solution>>>() {{
            if (cxOpNames.contains("scx")) {
                add((s1, s2) -> List.of(Crossovers.scx(s1, s2, run.problem)));
            }
            if (cxOpNames.contains("pmx")) {
                add((s1, s2) -> Crossovers.pmx(s1, s2, run.problem, random));
            }
           // if (cxOpNames.contains("aex")) {
           //     add((s1, s2) -> List.of(Crossover.aex(s1, s2, run.problem)));
           // }
        }};
        var mutOps = new ArrayList<Function<Solution, Solution>>() {{
            var mutOps = run.config.getMutationOperators();
            if (mutOps.contains("swap")) {
                add(s -> Mutator.singleSwap(s, random));
            }
            if (mutOps.contains("swap-segment")) {
                add(s -> Mutator.segmentSwap(s, random));
            }
            if (mutOps.contains("invert-segment")) {
                add(s -> Mutator.invertSwap(s, random));
            }
        }};

        /* Set up meta-recombinator and meta-mutator */
        recombinator = (s1, s2) -> {
            var cs = cxOps.get(random.nextInt(cxOps.size())).apply(s1, s2);
            cs.forEach(c -> Encoder.decodeSolution(c, problem));
            return cs;
        };
        mutator = (s1) -> {
            var s = mutOps.get(random.nextInt(mutOps.size())).apply(s1);
            Encoder.decodeSolution(s, problem);
            return s;
        };

        /* prepare main pop if necessary */
        Encoder.encodeSolutions(run.state.population, run.problem);
        state.population.getIndividuals()
            .forEach(s -> Evaluator.evaluate(s, problem));
        state.population.setIndividuals(
            Selectors.fastNonDominatedSort(
                state.population.getIndividuals(),
                state.population.getSize()
            )
        );
    }

    public void run() throws InterruptedException {
        while (config.getStopAfterGenerations() > state.generation
               && config.getStopAfterSteadyGenerations() > state.steadyGenerations
               && stopSignalSuppliers.stream().noneMatch(Supplier::get)) {
            state.generation += 1;

            /* 1. generate N children from population of N parents */
            List<Solution> children = new ArrayList<>();
            while (children.size() < state.population.getSize()) {
                var p1 = Selectors.select(state.population, 3, random);
                var p2 = Selectors.select(state.population, 3, random);
                var cs = recombinator.apply(p1, p2);
                if (random.nextDouble() < config.getMutationProbability()) {
                    cs.forEach(mutator::apply);
                }
                cs.forEach(s -> Evaluator.evaluate(s, problem));
                children.addAll(cs);
            }

            /* 2. combine children & parents into set R of size 2N */
            var mixed = new ArrayList<Solution>(state.population.getSize() + children.size());
            mixed.addAll(state.population.getIndividuals());
            mixed.addAll(children);

            var newPop = Selectors.fastNonDominatedSort(mixed, state.population.getSize());
           // System.out.println("new pop " + newPop.size());

            if (!Objects.equals(newPop.size(), state.population.getSize())) {
                throw new RuntimeException();
            }

            /* 3. check if there is improvement in optimum */
            // this is checked by non-dom sort between optimal solutions separates new front
            var oldFittest = state.population.getParettoFront();
            var newFittest = newPop.stream().filter(s -> Objects.equals(s.getRank(), 0)).toList();
            var combinedFittest = new ArrayList<Solution>() {{
                if (oldFittest != null) addAll(oldFittest);
                addAll(newFittest);
            }};

            /* Count steady generations */
            var twoBestFronts = Selectors.fastNonDominatedSort(combinedFittest, combinedFittest.size());
            int maxRank = twoBestFronts.stream().mapToInt(Solution::getRank).max().getAsInt();
            if (maxRank > 0) {
                state.steadyGenerations = 0;
            } else {
                state.steadyGenerations += 1;
            }

            state.population.setParettoFront(newFittest);
            state.population.setIndividuals(newPop);

            /* 3. notify listeners */
            iterConsumers.forEach(l -> l.accept(state));
        }
    }

    /* updated once per iteration */
    public void addIterationConsumer(Consumer<EvolutionState> listener) {
        iterConsumers.add(listener);
    }

    public EvolutionState getState() {
        return state;
    }

    /* read before entering next generation loop */
    public void addStopSignalSupplier(Supplier<Boolean> stopSignalSupplier) {
        stopSignalSuppliers.add(stopSignalSupplier);
    }
}
