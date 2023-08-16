package hr.fer.bernardcrnkovic.mtsp.operator;

import hr.fer.bernardcrnkovic.mtsp.model.Fitness;
import hr.fer.bernardcrnkovic.mtsp.model.Problem;
import hr.fer.bernardcrnkovic.mtsp.model.Solution;

import java.util.Objects;

public class Evaluator {
    public static Fitness evaluate(Solution sol, Problem prob) {
        if (sol.isEvaluated()) {
            return sol.getFitness();
        }
        var f = new Fitness();
        double totalLen = 0;
        double max = 0;
        for (var sal : sol.getPhenotype()) {
            if (Objects.equals(sal.getTour().length, 0)) {
                totalLen = Double.POSITIVE_INFINITY;
                max = Double.POSITIVE_INFINITY;
                break;
            }

            double tourLen = 0;
            var tour = sal.getTour();

            /* home -> start */
            totalLen += prob.distances[sal.getDepot()][tour[0]];

            /* in between */
            for (int i = 0; i < tour.length - 1; i++) {
                double cost = prob.distances[tour[i]][tour[(i+1) % tour.length]];
                totalLen += cost;
                tourLen += cost;
            }

            /* last -> home */
            totalLen += prob.distances[tour[tour.length - 1]][sal.getDepot()];

            /* check for max tour */
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
