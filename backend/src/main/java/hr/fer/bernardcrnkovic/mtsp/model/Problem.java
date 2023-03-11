package hr.fer.bernardcrnkovic.mtsp.model;

import java.util.Map;

public class Problem {
    public Integer id;
    public String label;
    public String color;
    public String description;
    public Map<Map.Entry<Integer, Integer>, Double> costs;

    /* Faster access */
    int numNodes;
    double[][] distances;
    boolean[][] present;
}
