package hr.fer.bernardcrnkovic.mtsp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import hr.fer.bernardcrnkovic.mtsp.model.Population;
import hr.fer.bernardcrnkovic.mtsp.model.Problem;
import hr.fer.bernardcrnkovic.mtsp.operator.Encoder;
import hr.fer.bernardcrnkovic.mtsp.operator.Crossovers;
import hr.fer.bernardcrnkovic.mtsp.operator.Evaluator;
import hr.fer.bernardcrnkovic.mtsp.operator.Mutator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

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
        Encoder.encodeSolutions(pop, prob);
        System.out.println("Dummy Depots: " + prob.dummyToRealDepot.keySet().stream().toList());
        // test crossover
        var sol1 = pop.getIndividuals().get(0);
        System.out.println("Parent 1: " + Arrays.toString(sol1.tours));
        var f1 = Evaluator.evaluate(sol1, prob);
        System.out.printf("Parent 1 fitness : (%s, %s)%n", f1.getTotalLength(), f1.getMaxTourLength());

        var sol2 = pop.getIndividuals().get(1);
        System.out.println("Parent 2: " + Arrays.toString(sol2.tours));
        var f2 = Evaluator.evaluate(sol2, prob);
        System.out.printf("Parent 2 fitness : (%s, %s)%n", f2.getTotalLength(), f2.getMaxTourLength());

        var child1 = Crossovers.scx(sol1, sol2, prob);
        System.out.println("Child   : " + Arrays.toString(child1.tours));
        var f3 = Evaluator.evaluate(child1, prob);
        System.out.printf("Child fitness : (%s, %s)%n", f3.getTotalLength(), f3.getMaxTourLength());

        var rand = new Random(6);
        for (int i = 0; i < pop.getSize(); i++) {
            for (int j = 0; j < pop.getSize(); j++) {
                var p1 = pop.getIndividuals().get(i);
                var p2 = pop.getIndividuals().get(j);
                var children = Crossovers.pmx(p1, p2, prob, rand);
                children.forEach(child -> {
                    System.out.println("Child   : " + Arrays.toString(child.tours));
                    var f = Evaluator.evaluate(child, prob);
                    System.out.printf("Child fitness : (%s, %s)%n", f.getTotalLength(), f.getMaxTourLength());
                    System.out.println(Set.of(Arrays.stream(child.tours).boxed().toArray()).size());
                });
            }
        }

        pop.getIndividuals().forEach(sol -> {
            System.out.println();
            System.out.println("original    : " + Arrays.toString(sol.tours));
            var s = sol.copy();
            s = Mutator.singleSwap(s, rand);
            System.out.println("single swap : " + Arrays.toString(s.tours));
            Set.of(s.tours);
            s = sol.copy();
            s = Mutator.segmentSwap(s, rand);
            System.out.println("segment swap: " + Arrays.toString(s.tours));
            Set.of(s.tours);
            s = sol.copy();
            s = Mutator.invertSwap(s, rand);
            System.out.println("seg. reverse: " + Arrays.toString(s.tours));
            Set.of(s.tours);
        });

        pop.getIndividuals().forEach(sol -> Evaluator.evaluate(sol, prob));
//        Selection.fastNonDominatedSort(pop.getIndividuals(), pop.getIndividuals().size()).forEach(front -> {
////            System.out.println("Front");
//            System.out.println(front.stream().map(s -> {
//                return "(%s, %s)".formatted(s.getTotalLength(), s.getMaxTourLength());
//            }).toList() + ", ");
//        });

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
