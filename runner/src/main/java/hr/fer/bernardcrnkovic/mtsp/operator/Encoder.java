package hr.fer.bernardcrnkovic.mtsp.operator;

import hr.fer.bernardcrnkovic.mtsp.model.Population;
import hr.fer.bernardcrnkovic.mtsp.model.Problem;
import hr.fer.bernardcrnkovic.mtsp.model.Salesman;
import hr.fer.bernardcrnkovic.mtsp.model.Solution;

import java.util.ArrayList;

public class Encoder {

    // embeds cache of solution encoding into single array representation
    // with dummy aliases for home depot with numbers AFTER last real depot
    public static void encodeSolutions(Population pop, Problem prob) {
        for (var sol : pop.getIndividuals()) {
            encodeSolution(sol, prob);
        }
    }

    public static void encodeSolution(Solution sol, Problem prob) {
        var dummyDepots = prob.dummyToRealDepot.keySet().stream().sorted().toArray(Integer[]::new);
//        System.out.println(Arrays.toString(dummyDepots));
        sol.tours = new int[prob.numNodes + dummyDepots.length - 1];
        int offset = 0;//prob.getDepots().length;
        int dummyDepotIndex = 0;
        var salesmen = sol.getPhenotype();
//        System.out.println(salesmen);
        for (Salesman sal : salesmen) {
            sol.tours[offset++] = dummyDepots[dummyDepotIndex++];
            System.arraycopy(sal.getTour(), 0, sol.tours, offset, sal.getTour().length);
//            System.out.println(Arrays.toString(sol.tours));
            offset += sal.getTour().length;
        }
//        System.out.println(Arrays.stream(sol.tours).boxed().collect(Collectors.toSet()).size());
    }

    public static void decodeSolution(Solution sol, Problem prob) {
        int i = 1;
        var pheno = new ArrayList<Salesman>();
        while (i < sol.tours.length) {
            var sal = new Salesman();
            if (!prob.dummyToRealDepot.containsKey(sol.tours[i - 1])) {
                throw new RuntimeException();
            }
            sal.setDepot(prob.dummyToRealDepot.get(sol.tours[i - 1]));
            int j = i;
            while (j < sol.tours.length && !prob.dummyToRealDepot.containsKey(sol.tours[j])) {
                j++;
            }
            var tour = new int[j - i];
            System.arraycopy(sol.tours, i, tour, 0, j - i);
            sal.setTour(tour);
            pheno.add(sal);
            i = j + 1;
        }
        sol.setPhenotype(pheno);
    }
}
