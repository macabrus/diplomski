package hr.fer.bernardcrnkovic.mtsp.algo;

import hr.fer.bernardcrnkovic.mtsp.model.EvolutionState;
import hr.fer.bernardcrnkovic.mtsp.model.Fitness;

import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.function.Consumer;

/* Consumes evolution on every step, should only READ data because it is being modified by evolution loop */
public class FitnessPublisher implements Consumer<EvolutionState> {
    private Random random = new Random();
    private int index = 0;
    private Fitness lastFitness = null;
    private Queue<Object> queue;

    public FitnessPublisher(Queue<Object> queue) {
        this.queue = queue;
    }

    @Override
    public void accept(EvolutionState evolutionState) {
        if (lastFitness != null /* and fitness is worse or equal to lastFitness */) {
            return;
        }
        queue.add(Map.of(
            "type", "update",
            "run_id", -1,
            "plot", "fitness",
            "index", index++,
            "value", random.nextInt(0, 100)
        ));
        // evolutionState
        // fitnessConsumer.accept();
    }
}
