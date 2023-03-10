package hr.fer.bernardcrnkovic.mtsp.model;

import java.util.Map;

public class Problem {
    Integer id;
    String label;
    String description;
    Map<Map.Entry<Integer, Integer>, Double> costs;

    /* Faster access */
    int numNodes;
    double[][] distances;
    boolean[][] present;
}
