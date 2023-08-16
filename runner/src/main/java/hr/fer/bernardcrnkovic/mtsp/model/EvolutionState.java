package hr.fer.bernardcrnkovic.mtsp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EvolutionState {
    @JsonProperty("seed")
    public long initialSeed;
    public int generation;
    public Population population;
    public int steadyGenerations;
}
