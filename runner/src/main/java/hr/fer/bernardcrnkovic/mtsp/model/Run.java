package hr.fer.bernardcrnkovic.mtsp.model;

import java.util.List;
import java.util.Map;

public class Run {
    public int id;
    public String label;
    public Problem problem; //
    public EvolutionState state; // mutable state
    public EvolutionConfig config; // static config
    public Map<String, List<Object>> metrics; // collected metrics for analysis
}
