package hr.fer.bernardcrnkovic.mtsp.model;

import java.util.HashMap;
import java.util.Map;

public class Problem {
    private Integer id;
    private String label;
    private String color;
    private String description;
    private Map<Map.Entry<Integer, Integer>, Double> costs = new HashMap<>();


    public Integer getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
    public Map<Map.Entry<Integer, Integer>, Double> getCosts() {
        return costs;
    }

    public String getDescription() {
        return description;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDistances(double[][] distances) {
        this.distances = distances;
    }
    
    /* Faster access */
    int numNodes;
    double[][] distances;
    boolean[][] present;
}
