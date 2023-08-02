package hr.fer.bernardcrnkovic.mtsp.operator;

import hr.fer.bernardcrnkovic.mtsp.model.Problem;
import hr.fer.bernardcrnkovic.mtsp.model.Salesman;
import hr.fer.bernardcrnkovic.mtsp.model.Solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;


public class Crossover {

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
            int j = (pivotIdx + 1) % len;
            while (visited[parent1[j % len]]) { j++;}
            int opt1 = parent1[j % len];

            // find opt2
            j = (cityToIndex2[pivot] + 1) % len;
            while (visited[parent2[j % len]]) { j++;}
            int opt2 = parent2[j % len];

            System.out.println();
            System.out.println("Options:");
            System.out.printf("%s -> %s%n", pivot, opt1);
            System.out.printf("%s -> %s%n", pivot, opt2);
            if (visited[opt1]) {
                System.out.printf("%s -> %s visited! Choosing other %n", pivot, opt1);
                child.tours[numVisited++] = opt2;
                pivotIdx = cityToIndex1[opt2];
                visited[opt2] = true;
            }
            else if (visited[opt2]) {
                System.out.printf("%s -> %s visited! Choosing other %n", pivot, opt2);
                child.tours[numVisited++] = opt1;
                pivotIdx = cityToIndex1[opt1];
                visited[opt1] = true;
            }
            else if (costs[pivot][opt1] >= costs[pivot][opt2]) {
                System.out.printf("cost(%s -> %s) = %s >= cost(%s -> %s) = %s %n", pivot, opt1, costs[pivot][opt1], pivot, opt2, costs[pivot][opt2]);
                child.tours[numVisited++] = opt2;
                pivotIdx = cityToIndex1[opt2];
                visited[opt2] = true;
            }
            else if (costs[pivot][opt2] > costs[pivot][opt1]){
                System.out.printf("cost(%s -> %s) = %s > cost(%s -> %s) = %s %n", pivot, opt2, costs[pivot][opt2], pivot, opt1, costs[pivot][opt1]);
                child.tours[numVisited++] = opt1;
                pivotIdx = cityToIndex1[opt1];
                visited[opt1] = true;
            }
            else {
                throw new RuntimeException();
            }
            System.out.println(Arrays.toString(child.tours));
        }
        System.out.println("PHASHSET SIZE " + Arrays.stream(parent1).boxed().collect(Collectors.toSet()).size());
        System.out.println("HASHSET SIZE " + Arrays.stream(child.tours).boxed().collect(Collectors.toSet()).size());

        // compute phenotype and store it for child
        int offset = 1;
        var pheno = new ArrayList<Salesman>();
        for (int i = 1; i < child.tours.length; i++) {
            if (prob.dummyToRealDepot.containsKey(child.tours[i])) {
                var sal = new Salesman();
                sal.setDepot(prob.dummyToRealDepot.get(child.tours[offset - 1]));
                var salTour = new int[i - offset];
                System.arraycopy(child.tours, offset, salTour, 0, i - offset);
                sal.setTour(salTour);
                pheno.add(sal);
            }
        }
        child.setPhenotype(pheno);

        return child;
    }

    public static int[] pmx(int[] parent1, int[] parent2, double[][] costs) {
        int chromosomeLength = parent1.length;
        int[] child = new int[chromosomeLength];

        // Select two random crossover points
        int point1 = (int) (Math.random() * chromosomeLength);
        int point2 = (int) (Math.random() * chromosomeLength);

        // Ensure point1 is smaller than point2
        if (point1 > point2) {
            int temp = point1;
            point1 = point2;
            point2 = temp;
        }

        // Copy the segment between the crossover points from parent1 to the child
        if (point2 + 1 - point1 >= 0) {
            System.arraycopy(parent1, point1, child, point1, point2 + 1 - point1);
        }

        // Fill the rest of the child chromosome using the PMX operator
        for (int i = 0; i < chromosomeLength; i++) {
            if (i >= point1 && i <= point2) {
                continue; // Skip the copied segment
            }

            int city = parent2[i];
            int childIndex = i;

            // Find a city from parent2 not present in the child chromosome
            while (isCityInChild(child, city, point1, point2)) {
                city = parent2[childIndex];
                childIndex = indexOf(parent1, city);
            }

            child[childIndex] = parent2[i];
        }

        return child;
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
