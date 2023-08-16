package hr.fer.bernardcrnkovic.mtsp.metric;

import hr.fer.bernardcrnkovic.mtsp.model.DataPoint;
import hr.fer.bernardcrnkovic.mtsp.model.EvolutionState;
import hr.fer.bernardcrnkovic.mtsp.model.Solution;

import java.time.Instant;
import java.util.Comparator;
import java.util.Map;

public class MetricExtractors {
    public static DataPoint extractParettoEdgeFitnesses(EvolutionState state){
        var mtSol = state.population.getParettoFront().stream()
            .max(Comparator.comparing(Solution::getMaxTourLength))
            .get();
        var tlSol = state.population.getParettoFront().stream()
            .max(Comparator.comparing(Solution::getTotalLength))
            .get();
        return new DataPoint(Instant.now(), Map.of(
            "generation", state.generation,
            "paretto_edge_1_max_tour", -mtSol.getMaxTourLength(),
            "paretto_edge_1_total_len", -mtSol.getTotalLength(),
            "paretto_edge_2_max_tour", -tlSol.getMaxTourLength(),
            "paretto_edge_2_total_len", -tlSol.getTotalLength()
        ));
    }
}
