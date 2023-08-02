package hr.fer.bernardcrnkovic.mtsp.operator;

import hr.fer.bernardcrnkovic.mtsp.model.Fitness;
import hr.fer.bernardcrnkovic.mtsp.model.Problem;
import hr.fer.bernardcrnkovic.mtsp.model.Solution;

public class FitnessUtils {
    public static Fitness computeFitness(Solution sol, Problem prob) {
        var f = new Fitness();
        double totalLen = 0;
        double max = 0;
        for (var sal : sol.getPhenotype()) {
            double tourLen = 0;
            var tour = sal.getTour();
            for (int i = 0; i < tour.length; i++) {
                double cost = prob.distances[tour[i]][tour[(i+1) % tour.length]];
                totalLen += cost;
                tourLen += cost;
            }
            if (tourLen > max) {
                max = tourLen;
            }
        }
        f.setMaxTourLength(-max);
        f.setTotalLength(-totalLen);
        sol.setFitness(f);
        sol.setEvaluated(true);
        return f;
    }
}
