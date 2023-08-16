package hr.fer.bernardcrnkovic.mtsp.operator;

import hr.fer.bernardcrnkovic.mtsp.model.Population;
import hr.fer.bernardcrnkovic.mtsp.model.Solution;

import java.util.*;
import java.util.stream.Collectors;

public class Selectors {

    public static final Comparator<Solution> crowdingDistanceComparator = (s1, s2) -> {
        if (!s1.isCrowdingDistanceEvaluated() || !s2.isCrowdingDistanceEvaluated()) {
            throw new RuntimeException();
        }
        int ranksCmp = Integer.compare(s1.getRank(), s2.getRank());
        if (ranksCmp < 0) { // better rank s1
            return 1;
        } else if (ranksCmp > 0) { // worse rank s1
            return -1;
        }
        var cdCmp = Double.compare(s1.getCrowdingDistance(), s2.getCrowdingDistance());
        if (cdCmp > 0) { // larger distance = better
            return 1;
        } else if (cdCmp < 0) {
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

    public static List<Solution> fastNonDominatedSort(List<Solution> pop, int cutoff) {
        int size = pop.size();
        var dominations = new HashMap<Integer, List<Integer>>() {{
            for (int i = 0; i < size; i++) {
                put(i, new ArrayList<>());
            }
        }};
        int[] dominatedByCount = new int[size]; // how many dominating solutions
        int totalSorted = 0;
        var fronts = new ArrayList<List<Integer>>() {{
            add(new ArrayList<>());
        }};
        for (int i = 0; i < size; i++) {
            var s1 = pop.get(i);
            for (int j = 0; j < size; j++) {
                var s2 = pop.get(j);
                if (dominates(s1, s2)) {
                    dominations.get(i).add(j);
                } else if (dominates(s2, s1)) {
                    dominatedByCount[i] += 1;
                }
            }
            if (Objects.equals(dominatedByCount[i], 0)) {
                totalSorted++;
                fronts.get(0).add(i);
            }
        }

        while (totalSorted < cutoff) {
            var front = new ArrayList<Integer>();
            for (int s1i : fronts.get(fronts.size() - 1)) {
                for (int s2i : dominations.get(s1i)) {
                    dominatedByCount[s2i] -= 1;
                    if (Objects.equals(dominatedByCount[s2i], 0)) {
                        totalSorted++;
                        front.add(s2i);
                    }
                }
            }
            fronts.add(front);
        }
        /* Take last front that was gathered,
           get most crowding-wise diverse offspring and keep it */
        var ret = fronts.stream()
                .map(front -> front.stream().map(pop::get).toList())
                .collect(Collectors.toList());

        /* set ranks for every node */
        for (int i = 0; i < ret.size(); i++) {
            var front = ret.get(i);
            for (var sol : front) {
                sol.setRank(i);
            }
            computeCrowdingDistances(front, 1);
        }

        /* remove excess from last front (the ones with smallest crowding distance) */
        var lastFront = ret.get(ret.size() - 1);
        lastFront = lastFront.stream()
                .sorted(crowdingDistanceComparator.reversed()) // reverse order of crowding operator
                .limit(lastFront.size() - (totalSorted - cutoff))  // last front without how much is too much
                .toList();
        ret.set(ret.size() - 1, lastFront);
        /* return flatmapped */
        return ret.stream().flatMap(Collection::stream).toList();
    }

    // computes quality in front based on how far from other solutions in that front
    // given solution is
    public static void computeCrowdingDistances(List<Solution> front, double sigma) {

        int len = front.size();

        var sortedByMaxTourSize = front.stream()
                .sorted(Comparator.comparing(Solution::getMaxTourLength))
                .toList();
        var sortedByTotalTourSize = front.stream()
                .sorted(Comparator.comparing(Solution::getTotalLength))
                .toList();

        // determine min/max bounds for relative scales
        double lbMaxSize = sortedByMaxTourSize.get(0).getMaxTourLength();
        double ubMaxSize = sortedByMaxTourSize.get(len - 1).getMaxTourLength();

        double lbTotal = sortedByTotalTourSize.get(0).getTotalLength();
        double ubTotal = sortedByTotalTourSize.get(len - 1).getTotalLength();

        // reset to 0 if set in past
        front.forEach(sol -> sol.setCrowdingDistanceEvaluated(false));

        // set to large number on edges and mark as evaluated
        sortedByMaxTourSize.get(0).setCrowdingDistance(10e9);
        sortedByMaxTourSize.get(len - 1).setCrowdingDistance(10e9);
        sortedByTotalTourSize.get(0).setCrowdingDistance(10e9);
        sortedByTotalTourSize.get(len - 1).setCrowdingDistance(10e9);

        for (int i = 1; i < len - 1; i++) {
            // criteria 1
            var sol = sortedByMaxTourSize.get(i);
            sol.setCrowdingDistance(
                sol.getCrowdingDistance() +
                (sortedByMaxTourSize.get(i + 1).getMaxTourLength()
                - sortedByMaxTourSize.get(i - 1).getMaxTourLength()) /
                (ubMaxSize - lbMaxSize)
            );

            // criteria 2
            sol = sortedByTotalTourSize.get(i);
            sol.setCrowdingDistance(
                sol.getCrowdingDistance() +
                (sortedByTotalTourSize.get(i + 1).getMaxTourLength()
                - sortedByTotalTourSize.get(i - 1).getMaxTourLength()) /
                (ubTotal - lbTotal)
            );
        }
        front.forEach(s -> s.setCrowdingDistanceEvaluated(true));
    }

    // call after non-dom sorting
    // this uses crowding comparison operator
    public static Solution select(Population population, int k, Random rand) {
        var pop = population.getIndividuals();
        return rand.ints(k, 0, pop.size())
                .mapToObj(pop::get).max(crowdingDistanceComparator).get();
    }
}
