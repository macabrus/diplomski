package hr.fer.bernardcrnkovic.mtsp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Problem {
    private Integer id;
    private String label;
    private String color;
    private String description;
    private double[][] costs;
    private int numSalesmen;
    private int[] depots; // SINGLE DEPOT ONLY

    // map (dummy depot) => real depot
    @JsonIgnore
    public Map<Integer, Integer> dummyToRealDepot = new HashMap<>();
    public Map<String, double[]> display;

    /* For faster access */
    @JsonIgnore
    public int numNodes;
    @JsonIgnore
    public double[][] distances; // augmented cost matrix
    public boolean[][] present;

    public Integer getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getColor() {
        return color;
    }

    public double[][] getCosts() {
        return costs;
    }

    public String getDescription() {
        return description;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public void setColor(String color) {
        this.color = color;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int[] getDepots() {
        return depots;
    }

    public void setDepots(int[] depots) {
        this.depots = depots;
        recomputeDistances();
    }

    public Map<String, double[]> getDisplay() {
        return display;
    }

    public void setDisplay(Map<String, double[]> display) {
        this.display = display;
    }

    private void recomputeDistances() {
        if (costs == null || depots == null) {
            return;
        }
        int realNodes = costs.length;
        numNodes = realNodes; // remember num nodes
        /* Number of nodes when counting dummy depots */
        /* one real depot can be used and other salesmen need fake one */
        int allNodes = realNodes + numSalesmen - 1;
        //        System.out.println(allNodes);
        Arrays.stream(depots).forEach(v -> dummyToRealDepot.put(v, v));
        for (int i = realNodes, j = 0; i < allNodes; i++) {
            dummyToRealDepot.put(i, depots[0]);
        }
        //        System.out.println(dummyToRealDepot);
        if (depots.length > 1) {
            throw new RuntimeException("Expected exactly 1 depot.");
        }
        distances = new double[allNodes][allNodes];
        // Copy Upper left portion of matrix
        for (int i = 0; i < costs.length; i++) {
            //            System.out.println(Arrays.deepToString(distances));
            System.arraycopy(costs[i], 0, distances[i], 0, costs[0].length);
        }
        // Augment with dummy depots
        for (int key : dummyToRealDepot.keySet()) {
            // fill right columns
            for (int j = 0; j < costs.length; j++) {
                if (Objects.equals(depots[0], j)) {
                    distances[j][key] = Double.POSITIVE_INFINITY;
                } else {
                    distances[j][key] = costs[j][depots[0]];
                }
            }
            // fill bottom rows
            for (int j = 0; j < costs[0].length; j++) {
                if (Objects.equals(depots[0], j)) {
                    distances[key][j] = Double.POSITIVE_INFINITY;
                } else {
                    distances[key][j] = costs[j][depots[0]];
                }
            }
        }
    }

    public void setCosts(double[][] costs) {
        this.costs = costs;
        recomputeDistances();
    }

    public void setNumSalesmen(int numSalesmen) {
        this.numSalesmen = numSalesmen;
    }

    public int getNumSalesmen() {
        return numSalesmen;
    }

}
