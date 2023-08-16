package hr.fer.bernardcrnkovic.mtsp.operator;

import hr.fer.bernardcrnkovic.mtsp.model.Problem;
import hr.fer.bernardcrnkovic.mtsp.model.Solution;

import java.util.*;


public class Crossovers {

    // sequential cycle crossover
    public static Solution scx(Solution sol2, Solution sol1, Problem prob) {
        // cached fast access variables
        var parent1 = sol1.tours;
        var parent2 = sol2.tours;
        int len = parent1.length;

        var child = new Solution();
        child.tours = new int[len];
        var costs = prob.distances;

        // single home depot so +1, 0th index will remain unset
        var cityToIndex1 = new int[len];
        var cityToIndex2 = new int[len];

        // from (city index) => city...
        // initialize lookup (city) => city index
        for (int i = 0; i < len; i++) {
            cityToIndex1[parent1[i]] = i;
            cityToIndex2[parent2[i]] = i;
        }

        // marks for visited cities
        var visited = new boolean[len];
        visited[0] = true;
        int numVisited = 1;

        int pivotIdx = 0;
        while (numVisited != len) {
            int pivot = parent1[pivotIdx];

            // find opt1
            int j = pivotIdx + 1;
            while (visited[parent1[j % len]]) { j++;}
            int opt1 = parent1[j % len];

            // find opt2
            j = cityToIndex2[pivot] + 1;
            while (visited[parent2[j % len]]) { j++;}
            int opt2 = parent2[j % len];

//            System.out.println();
//            System.out.println("Options:");
//            System.out.printf("%s -> %s%n", pivot, opt1);
//            System.out.printf("%s -> %s%n", pivot, opt2);
            if (visited[opt1]) {
//                System.out.printf("%s -> %s visited! Choosing other %n", pivot, opt1);
                child.tours[numVisited++] = opt2;
                pivotIdx = cityToIndex1[opt2];
                visited[opt2] = true;
            }
            else if (visited[opt2]) {
//                System.out.printf("%s -> %s visited! Choosing other %n", pivot, opt2);
                child.tours[numVisited++] = opt1;
                pivotIdx = cityToIndex1[opt1];
                visited[opt1] = true;
            }
            else if (costs[pivot][opt1] >= costs[pivot][opt2]) {
//                System.out.printf("cost(%s -> %s) = %s >= cost(%s -> %s) = %s %n", pivot, opt1, costs[pivot][opt1], pivot, opt2, costs[pivot][opt2]);
                child.tours[numVisited++] = opt2;
                pivotIdx = cityToIndex1[opt2];
                visited[opt2] = true;
            }
            else if (costs[pivot][opt2] > costs[pivot][opt1]){
//                System.out.printf("cost(%s -> %s) = %s > cost(%s -> %s) = %s %n", pivot, opt2, costs[pivot][opt2], pivot, opt1, costs[pivot][opt1]);
                child.tours[numVisited++] = opt1;
                pivotIdx = cityToIndex1[opt1];
                visited[opt1] = true;
            }
            else {
                throw new RuntimeException();
            }
//            System.out.println(Arrays.toString(child.tours));
        }
//        System.out.println("PHASHSET SIZE " + Arrays.stream(parent1).boxed().collect(Collectors.toSet()).size());
//        System.out.println("HASHSET SIZE " + Arrays.stream(child.tours).boxed().collect(Collectors.toSet()).size());

        // cache phenotype and store it for child
        Encoder.decodeSolution(child, prob);
        return child;
    }

    // partially mapped crossover
    public static List<Solution> pmx(Solution p1, Solution p2, Problem prob, Random rand) {
//        System.out.println("--- PMX BEGING ---");
//        System.out.println("P1: " + Arrays.toString(p1.tours));
//        System.out.println("P2: " + Arrays.toString(p2.tours));

        int len = p1.tours.length;

        var c1 = new Solution();
        var c2 = new Solution();
        c1.tours = new int[len];
        c2.tours = new int[len];

        // Select two random crossover points
        int x1 = rand.nextInt(1, len);
        int x2 = rand.nextInt(1, len);

        // Ensure point1 is smaller than point2
        if (x1 > x2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }
//        System.out.printf("Cut-point: %s ... %s%n", x1, x2);

        var mapC1 = new HashMap<Integer, Integer>();
        var mapC2 = new HashMap<Integer, Integer>();
        // copy strips to opposite children
        for (int i = x1; i < x2 + 1; i++) {
            c1.tours[i] = p1.tours[i];
            c2.tours[i] = p2.tours[i];
            mapC1.put(c1.tours[i], c2.tours[i]);
            mapC2.put(c2.tours[i], c1.tours[i]);
        }
//        System.out.println("Map for C1: " + mapC1);
//        System.out.println("Map for C2: " + mapC2);

        // Fill the rest of the child chromosome using the PMX operator
        for (int i = 0; i < len; i++) {
            if (i >= x1 && i <= x2) {
                continue; // Skip the copied segment
            }
            var pivotC1 = p2.tours[i];
            while(mapC1.containsKey(pivotC1)) {
                pivotC1 = mapC1.get(pivotC1);
            }
            c1.tours[i] = pivotC1;

            var pivotC2 = p1.tours[i];
            while(mapC2.containsKey(pivotC2)) {
                pivotC2 = mapC2.get(pivotC2);
            }
            c2.tours[i] = pivotC2;
        }

//        System.out.println("A " + Arrays.toString(c1.tours));
//        System.out.println("A " + Arrays.toString(c2.tours));
        Encoder.decodeSolution(c1, prob);
        Encoder.decodeSolution(c2, prob);
//        System.out.println("C1: " + Arrays.toString(c1.tours));
//        System.out.println("C2: " + Arrays.toString(c2.tours));
//
//        System.out.println("--- PMX END ---");
        return List.of(c1, c2);
    }

    public static Solution aex(Solution s1, Solution s2, Problem problem) {
        // 1. alternate inherited edges until possible without closing cycle
        // 2. if cycle would be closed too early, inherit any non-cycle-closing edge randomly
        // 3. repeat steps until last city
        // 4. copy last city manually
        return new Solution();
    }

    private static boolean isCityInChild(int[] child, int city, int point1, int point2) {
        for (int i = point1; i <= point2; i++) {
            if (child[i] == city) {
                return true;
            }
        }
        return false;
    }

    private static int indexOf(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }
}
