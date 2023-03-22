package hr.fer.bernardcrnkovic.mtsp.metric;

import hr.fer.bernardcrnkovic.mtsp.model.EvolutionState;

import java.time.Instant;
import java.util.function.Consumer;

public class GenerationTimeAccumulator implements Consumer<EvolutionState> {
    private Instant lastIterTimestamp;

    @Override
    public void accept(EvolutionState evolutionState) {
        if (lastIterTimestamp == null) {
            // lastIterTimestamp =
        }
    }
}
