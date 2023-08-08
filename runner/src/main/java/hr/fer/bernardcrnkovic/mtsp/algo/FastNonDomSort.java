package hr.fer.bernardcrnkovic.mtsp.algo;

import hr.fer.bernardcrnkovic.mtsp.model.Population;
import hr.fer.bernardcrnkovic.mtsp.model.Solution;

import java.util.*;

public class FastNonDomSort {

    public static final Comparator<Solution> crowdingDistanceComparator = (s1, s2) -> {
        int ranksComp = Integer.compare(s1.getRank(), s2.getRank());
        if (ranksComp < 0) {
            return 1;
        } else if (ranksComp > 0) {
            return -1;
        }
        var cd = Double.compare(s1.getCrowdingDistance(), s2.getCrowdingDistance());
        if (cd > 0) {
            return 1;
        } else if (cd < 0) {
            return -1;
        }
        return 0;
    };

    private static boolean dominates(Solution s1, Solution s2) {
        if (!s1.isEvaluated() || !s2.isEvaluated()) {
            throw new RuntimeException();
        }
        // dominira ako:
        // 1. u svim komponentama bolje ili jednako
        return (s1.getMaxTourLength() >= s2.getMaxTourLength()) && (s1.getTotalLength() >= s2.getTotalLength()) &&
        // 2. u barem jednoj komponenti strogo bolje
        (s1.getMaxTourLength() > s2.getMaxTourLength() || s1.getTotalLength() > s2.getMaxTourLength());
    }

    public static List<List<Solution>> fastNonDominatedSort(List<Solution> pop, int cutoff) {
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
        return fronts.stream().map(front -> front.stream().map(pop::get).sorted(
                Comparator.comparing(Solution::getTotalLength).thenComparing(Solution::getMaxTourLength)
        ).toList()).toList();
    }

    // computes quality in front based on how far from other solutions in that front
    // given solution is
    public static void computeCrowdingDistances(List<Solution> front, double sigma) {
        var sortedByMaxTourSize = front.stream()
                .sorted(Comparator.comparing(Solution::getMaxTourLength))
                .toList();
        var sortedByTotalTourSize = front.stream()
                .sorted(Comparator.comparing(Solution::getTotalLength))
                .toList();

        // determine min/max bounds for relative scales
        double lbMaxSize = sortedByMaxTourSize.get(0).getMaxTourLength();
        double ubMaxSize = sortedByMaxTourSize.get(front.size() - 1).getMaxTourLength();

        double lbTotal = sortedByTotalTourSize.get(0).getTotalLength();
        double ubTotal = sortedByTotalTourSize.get(front.size() - 1).getTotalLength();

        // reset to 0 if set in past
        front.forEach(sol -> sol.setCrowdingDistance(0));

        // set to large number on edges
        sortedByMaxTourSize.get(0).setCrowdingDistance(10e9);
        sortedByMaxTourSize.get(front.size() - 1).setCrowdingDistance(10e9);
        sortedByTotalTourSize.get(0).setCrowdingDistance(10e9);
        sortedByTotalTourSize.get(front.size() - 1).setCrowdingDistance(10e9);

        for (int i = 1; i < front.size() - 1; i++) {
            var sol = sortedByMaxTourSize.get(i);
            sol.setCrowdingDistance(
                sol.getCrowdingDistance() +
                (sortedByMaxTourSize.get(i + 1).getMaxTourLength()
                - sortedByMaxTourSize.get(i - 1).getMaxTourLength()) /
                (ubMaxSize - lbMaxSize)
            );
            sol = sortedByTotalTourSize.get(i);
            sol.setCrowdingDistance(
                sol.getCrowdingDistance() +
                (sortedByTotalTourSize.get(i + 1).getMaxTourLength()
                - sortedByTotalTourSize.get(i - 1).getMaxTourLength()) /
                (ubTotal - lbTotal)
            );
        }
    }

    // call after non-dom sorting
    // this uses crowding comparison operator
    public static Solution select(Population population, int k, Random rand) {
        var pop = population.getIndividuals();
        return rand.ints(k).mapToObj(pop::get).max(crowdingDistanceComparator).get();
    }
}
