package hr.fer.bernardcrnkovic.mtsp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class Solution {
    List<Salesman> phenotype;

    // faster access
    public int[] tours;

    private Fitness fitness;
    /* non-serialized properties */
    int rank;
    double crowdingDistance;
    private boolean evaluated = false;
    private boolean crowdingDistanceEvaluated = false;

    public List<Salesman> getPhenotype() {
        return phenotype;
    }

    public void setPhenotype(List<Salesman> phenotype) {
        this.phenotype = phenotype;
    }

    @JsonIgnore
    public int[] getTours() {
        return tours;
    }

    @JsonIgnore
    public void setTours(int[] tours) {
        this.tours = tours;
    }

    // total tours sizes added up
    @JsonIgnore
    public double getTotalLength() {
        return fitness.getTotalLength();
    }

    // longest tour size
    @JsonIgnore
    public double getMaxTourLength() {
        return fitness.getMaxTourLength();
    }

    // computed distance to closest neighbors normalized
    @JsonIgnore
    public double getCrowdingDistance() {
        return crowdingDistance;
    }

    public void setCrowdingDistance(double crowdingDistance) {
        this.crowdingDistance = crowdingDistance;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    // which front it belongs to (lower is better)
    public int getRank() {
        return rank;
    }

    public void setCrowdingDistanceEvaluated(boolean crowdingDistanceEvaluated) {
        this.crowdingDistanceEvaluated = crowdingDistanceEvaluated;
    }

    @JsonIgnore
    public boolean isCrowdingDistanceEvaluated() {
        return crowdingDistanceEvaluated;
    }

    @JsonIgnore
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
        sol.rank = this.rank;
        System.arraycopy(tours, 0, sol.tours, 0, tours.length);
        return sol;
    }
}
