package hr.fer.bernardcrnkovic.mtsp.model;

import java.util.List;

public class EvolutionConfig {
    private float mutationProbability;
    private float sharingDistance;
    private Integer stopAfterGenerations;
    private boolean monitorSteadyGenerations;
    private int stopAfterSteadyGenerations;
    private List<String> mutationOperators;
    private List<String> crossoverOperators;

    public float getMutationProbability() {
        return mutationProbability;
    }

    public void setMutationProbability(float mutationProbability) {
        this.mutationProbability = mutationProbability;
    }

    public float getSharingDistance() {
        return sharingDistance;
    }

    public void setSharingDistance(float sharingDistance) {
        this.sharingDistance = sharingDistance;
    }

    public Integer getStopAfterGenerations() {
        return stopAfterGenerations;
    }

    public void setStopAfterGenerations(Integer stopAfterGenerations) {
        this.stopAfterGenerations = stopAfterGenerations;
    }

    public int getStopAfterSteadyGenerations() {
        return stopAfterSteadyGenerations;
    }

    public void setStopAfterSteadyGenerations(int stopAfterSteadyGenerations) {
        this.stopAfterSteadyGenerations = stopAfterSteadyGenerations;
    }

    public List<String> getMutationOperators() {
        return mutationOperators;
    }

    public void setMutationOperators(List<String> mutationOperators) {
        this.mutationOperators = mutationOperators;
    }

    public List<String> getCrossoverOperators() {
        return crossoverOperators;
    }

    public void setCrossoverOperators(List<String> crossoverOperators) {
        this.crossoverOperators = crossoverOperators;
    }

    public boolean isMonitorSteadyGenerations() {
        return monitorSteadyGenerations;
    }

    public void setMonitorSteadyGenerations(boolean monitorSteadyGenerations) {
        this.monitorSteadyGenerations = monitorSteadyGenerations;
    }
}
