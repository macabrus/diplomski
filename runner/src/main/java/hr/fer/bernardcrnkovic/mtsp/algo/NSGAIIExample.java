package hr.fer.bernardcrnkovic.mtsp.algo;

import java.util.*;

public class NSGAIIExample {

    public static List<List<Map.Entry<Double, Double>>> nonDominatedSorting(List<Map.Entry<Double, Double>> entries) {
        List<List<Map.Entry<Double, Double>>> fronts = new ArrayList<>();
        List<Set<Map.Entry<Double, Double>>> dominatedSetList = new ArrayList<>();
        Map<Map.Entry<Double, Double>, Integer> rankMap = new HashMap<>();

        for (Map.Entry<Double, Double> entry : entries) {
            Set<Map.Entry<Double, Double>> dominatedSet = new HashSet<>();
            int dominatedByCount = 0;

            for (Map.Entry<Double, Double> other : entries) {
                if (entry == other) continue;

                if (isDominated(entry, other)) {
                    dominatedSet.add(other);
                } else if (isDominated(other, entry)) {
                    dominatedByCount++;
                }
            }

            if (dominatedByCount == 0) {
                rankMap.put(entry, 1);
                fronts.add(new ArrayList<>(Collections.singletonList(entry)));
                dominatedSetList.add(dominatedSet);
            }
        }

        int frontIndex = 0;
        while (!fronts.get(frontIndex).isEmpty()) {
            List<Map.Entry<Double, Double>> nextFront = new ArrayList<>();
            for (Map.Entry<Double, Double> entry : fronts.get(frontIndex)) {
                Set<Map.Entry<Double, Double>> dominatedSet = dominatedSetList.get(frontIndex);

                for (Map.Entry<Double, Double> other : dominatedSet) {
                    Set<Map.Entry<Double, Double>> newDominatedSet = new HashSet<>(dominatedSetList.get(rankMap.get(other) - 1));
                    newDominatedSet.remove(entry);
                    int dominatedByCount = 0;

                    for (Map.Entry<Double, Double> other2 : entries) {
                        if (entry == other2) continue;

                        if (isDominated(entry, other2)) {
                            newDominatedSet.add(other2);
                        } else if (isDominated(other2, entry)) {
                            dominatedByCount++;
                        }
                    }

                    if (dominatedByCount == 0) {
                        rankMap.put(entry, frontIndex + 2);
                        nextFront.add(entry);
                        dominatedSetList.set(frontIndex, newDominatedSet);
                    }
                }
            }

            frontIndex++;
            fronts.add(nextFront);
        }

        fronts.remove(fronts.size() - 1); // Remove the last empty front
        return fronts;
    }

    private static boolean isDominated(Map.Entry<Double, Double> a, Map.Entry<Double, Double> b) {
        return a.getKey() < b.getKey() && a.getValue() < b.getValue();
    }

    // Test the non-dominated sorting algorithm
    public static void main(String[] args) {
        List<Map.Entry<Double, Double>> entries = new ArrayList<>();
        entries.add(new AbstractMap.SimpleEntry<>(3.0, 4.0));
        entries.add(new AbstractMap.SimpleEntry<>(2.0, 5.0));
        entries.add(new AbstractMap.SimpleEntry<>(4.0, 3.0));
        entries.add(new AbstractMap.SimpleEntry<>(1.0, 6.0));
        entries.add(new AbstractMap.SimpleEntry<>(5.0, 2.0));

        List<List<Map.Entry<Double, Double>>> fronts = nonDominatedSorting(entries);

        // Print the results
        for (int i = 0; i < fronts.size(); i++) {
            System.out.println("Front " + (i + 1) + ":");
            for (Map.Entry<Double, Double> entry : fronts.get(i)) {
                System.out.println(entry.getKey() + ", " + entry.getValue());
            }
            System.out.println();
        }
    }
}
