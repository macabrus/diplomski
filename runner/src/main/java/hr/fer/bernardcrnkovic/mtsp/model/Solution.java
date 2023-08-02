package hr.fer.bernardcrnkovic.mtsp.model;

import java.util.List;

public class Solution {
    List<Salesman> phenotype;
    Fitness fitness;
    boolean evaluated = false;

    // faster access
    public int[] tours;

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

    public Fitness getFitness() {
        return fitness;
    }

    public void setFitness(Fitness fitness) {
        evaluated = fitness != null;
        this.fitness = fitness;
    }

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean b) {
        this.evaluated = b;
    }
}
