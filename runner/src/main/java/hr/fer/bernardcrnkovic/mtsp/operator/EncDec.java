package hr.fer.bernardcrnkovic.mtsp.operator;

import hr.fer.bernardcrnkovic.mtsp.model.Population;
import hr.fer.bernardcrnkovic.mtsp.model.Problem;
import hr.fer.bernardcrnkovic.mtsp.model.Salesman;
import hr.fer.bernardcrnkovic.mtsp.model.Solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EncDec {


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
        int offset = 1;
        var pheno = new ArrayList<Salesman>();
        for (int i = 1; i < sol.tours.length; i++) {
            if (prob.dummyToRealDepot.containsKey(sol.tours[i])) {
                var sal = new Salesman();
//                System.out.println(Arrays.toString(sol.tours));
//                System.out.println(prob.dummyToRealDepot);
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
