package hr.fer.bernardcrnkovic.mtsp.algo;

import hr.fer.bernardcrnkovic.mtsp.model.Population;
import hr.fer.bernardcrnkovic.mtsp.model.Run;
import hr.fer.bernardcrnkovic.mtsp.model.Solution;

import java.util.*;

public class FastNonDomSort {

    private static boolean dominates(Solution s1, Solution s2) {
        var f1 = s1.getFitness();
        var f2 = s2.getFitness();
        if (!s1.isEvaluated() || !s2.isEvaluated()) {
            throw new RuntimeException();
        }
        var val = (f1.getMaxTourLength() > f2.getMaxTourLength()) && (f1.getTotalLength() > f2.getTotalLength());
//        System.out.println("(%s, %s) > (%s, %s)? %s".formatted(f1.getTotalLength(), f1.getMaxTourLength(), f2.getTotalLength(), f2.getMaxTourLength(), val));
        return val;
    }

    public static List<List<Solution>> fastNonDominatedSort(Population population) {
        var pop = population.getIndividuals();
        int size = pop.size();
        var dominations = new HashMap<Integer, List<Integer>>() {{
            for (int i = 0; i < size; i++) {
                put(i, new ArrayList<>());
            }
        }};
        int[] dominatedByCount = new int[size]; // how many dominating solutions
        var fronts = new ArrayList<List<Integer>>() {{add(new ArrayList<>());}};
        for(int i = 0; i < size - 1; i++) {
            var s1 = pop.get(i);
            for (int j = i + 1; j < size; j++) {
                var s2 = pop.get(j);
                if (dominates(s1, s2)) {
                    dominations.get(i).add(j);
                    dominatedByCount[j] += 1;
                }
                else if (dominates(s2, s1)) {
                    dominations.get(j).add(i);
                    dominatedByCount[i] += 1;
                }
            }
            if (Objects.equals(dominatedByCount[i], 0)) {
                fronts.get(0).add(i);
            }
        }

        while (!fronts.get(fronts.size() - 1).isEmpty()) {
            var front = new ArrayList<Integer>();
            for (int s1i : fronts.get(fronts.size() - 1)) {

                for (int s2i : dominations.get(s1i)) {
                    dominatedByCount[s2i] -= 1;
                    if (Objects.equals(dominatedByCount[s2i], 0)) {
                        front.add(s2i);
                    }
                }
            }
            fronts.add(front);
        }
        return fronts.stream().map(front -> front.stream().map(pop::get).toList()).toList();
    }
}
