package hr.fer.bernardcrnkovic.mtsp.operator;

import hr.fer.bernardcrnkovic.mtsp.model.Population;
import hr.fer.bernardcrnkovic.mtsp.model.Problem;
import hr.fer.bernardcrnkovic.mtsp.model.Salesman;
import hr.fer.bernardcrnkovic.mtsp.model.Solution;

import java.util.ArrayList;
import java.util.Arrays;

public class Compact {

    public static void cacheProblemDistances(Problem p) {

    }

    // embeds cache of solution encoding into single array representation
    // with dummy aliases for home depot with numbers AFTER last real depot
    public static void cacheSolutionSequence(Problem prob, Population pop) {
        for (var sol : pop.getIndividuals()) {
            // tours = num all nodes - 1 + all depots - 1 because home is not stored there
            var dummyDepots = prob.dummyToRealDepot.keySet().stream()
                    .filter(d -> Arrays.stream(prob.getDepots()).noneMatch(d::equals))
                    .toArray(Integer[]::new);
            sol.tours = new int[prob.numNodes + dummyDepots.length];
            System.arraycopy(prob.getDepots(), 0, sol.tours, 0, prob.getDepots().length);
            int offset = prob.getDepots().length;
//            System.out.println(Arrays.toString(dummyDepots));
            int dummyDepotIndex = 0;
            var salesmen = sol.getPhenotype();
//            System.out.println(salesmen);
            for (int i = 0; i < salesmen.size(); i++) {
                var sal = salesmen.get(i);
                System.arraycopy(sal.getTour(), 0, sol.tours, offset, sal.getTour().length);
                offset += sal.getTour().length;
//                System.out.println(Arrays.toString(prob.getDepots()));
                if (i == salesmen.size() - 1) {
                    break;
                }
//                System.out.println(Arrays.toString(sol.tours));
                sol.tours[offset++] = dummyDepots[dummyDepotIndex++];
            }
//            System.out.println(Arrays.toString(sol.tours));
        }
    }

    public static void computeSalesmenFromGene(Solution sol, Problem prob) {
        int offset = 1;
        var pheno = new ArrayList<Salesman>();
        for (int i = 1; i < sol.tours.length; i++) {
            if (prob.dummyToRealDepot.containsKey(sol.tours[i])) {
                var sal = new Salesman();
                sal.setDepot(prob.dummyToRealDepot.get(sol.tours[offset - 1]));
                var salTour = new int[i - offset];
                System.arraycopy(sol.tours, offset, salTour, 0, i - offset);
                sal.setTour(salTour);
                pheno.add(sal);
            }
        }
        sol.setPhenotype(pheno);
    }
}
