package hr.fer.bernardcrnkovic.mtsp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import hr.fer.bernardcrnkovic.mtsp.algo.FastNonDomSort;
import hr.fer.bernardcrnkovic.mtsp.model.Population;
import hr.fer.bernardcrnkovic.mtsp.model.Problem;
import hr.fer.bernardcrnkovic.mtsp.operator.Compact;
import hr.fer.bernardcrnkovic.mtsp.operator.Crossover;
import hr.fer.bernardcrnkovic.mtsp.operator.FitnessUtils;

import java.io.IOException;
import java.util.Arrays;

public class Parsing {
    public static void main(String[] args) throws IOException {
        // deser problem
        ObjectMapper mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        var prob = mapper.readValue(Parsing.class.getResourceAsStream("/bayg29.json"), Problem.class);
        //printMatrix(prob.distances);

        // deser population
        var pop = mapper.readValue(Parsing.class.getResourceAsStream("/bayg29-population.json"), Population.class);
        // set prob
        Compact.cacheSolutionSequence(prob, pop);
        System.out.println("Dummy Depots: " + prob.dummyToRealDepot.keySet().stream().toList());
        // test crossover
        var sol1 = pop.getIndividuals().get(0);
        System.out.println("Parent 1: " + Arrays.toString(sol1.tours));
        var f1 = FitnessUtils.computeFitness(sol1, prob);
        System.out.printf("Parent 1 fitness : (%s, %s)%n", f1.getTotalLength(), f1.getMaxTourLength());

        var sol2 = pop.getIndividuals().get(1);
        System.out.println("Parent 2: " + Arrays.toString(sol2.tours));
        var f2 = FitnessUtils.computeFitness(sol2, prob);
        System.out.printf("Parent 2 fitness : (%s, %s)%n", f2.getTotalLength(), f2.getMaxTourLength());

        var child = Crossover.scx(sol1, sol2, prob);
        System.out.println("Child   : " + child);
        var f3 = FitnessUtils.computeFitness(child, prob);
        System.out.printf("Child fitness : (%s, %s)%n", f3.getTotalLength(), f3.getMaxTourLength());

        pop.getIndividuals().forEach(sol -> FitnessUtils.computeFitness(sol, prob));
        FastNonDomSort.fastNonDominatedSort(pop).forEach(front -> {
            System.out.println("Front");
            System.out.println(front.stream().map(s -> {
                var f = s.getFitness();
                return "(%s, %s)".formatted(f.getTotalLength(), f.getMaxTourLength());
            }).toList());
        });

    }

    public static void printMatrix(double[][] matrix) {
        for (double[] nums : matrix) {
            for (double num : nums) {
                System.out.printf("%10.0f", num);
            }
            System.out.println();
        }
    }


}
