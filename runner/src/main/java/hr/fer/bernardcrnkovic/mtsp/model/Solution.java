package hr.fer.bernardcrnkovic.mtsp.model;

import java.util.List;

public class Solution {
    List<Salesman> phenotype;

    // faster access
    public int[] tours;

    private Fitness fitness;
    /* non-serialized properties */
    int front;
    double crowdingDistance;

    public List<Salesman> getPhenotype() {
        return phenotype;
    }

    public void setPhenotype(List<Salesman> phenotype) {
        this.phenotype = phenotype;
    }

    public int[] getTours() {
        return tours;
    }

    public void setTours(int[] tours) {
        this.tours = tours;
    }

    // total tours sizes added up
    public double getTotalLength() {
        return fitness.getTotalLength();
    }

    // longest tour size
    public double getMaxTourLength() {
        return fitness.getMaxTourLength();
    }

    // computed distance to closest neighbors normalized
    public double getCrowdingDistance() {
        return crowdingDistance;
    }

    public void setCrowdingDistance(double crowdingDistance) {
        this.crowdingDistance = crowdingDistance;
    }

    // which front it belongs to (lower is better)
    public int getRank() {
        return front;
    }

    private boolean evaluated = false;

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean b) {
        this.evaluated = b;
    }

    public Fitness getFitness() {
        return fitness;
    }

    public void setFitness(Fitness fitness) {
        this.fitness = fitness;
    }

    public Solution copy() {
        var sol = new Solution();
        sol.phenotype = this.phenotype.stream().map(sal -> {
            var newSal = new Salesman();
            newSal.depot = sal.depot;
            newSal.tour = new int[sal.tour.length];
            System.arraycopy(sal.tour, 0, newSal.tour, 0, newSal.tour.length);
            return newSal;
        }).toList();
        sol.tours = new int[tours.length];
        sol.evaluated = this.evaluated;

        sol.fitness = new Fitness();
        sol.fitness.maxTourLength = this.getMaxTourLength();
        sol.fitness.totalLength = this.getTotalLength();

        sol.crowdingDistance = this.crowdingDistance;
        sol.front = this.front;
        System.arraycopy(tours, 0, sol.tours, 0, tours.length);
        return sol;
    }
}
