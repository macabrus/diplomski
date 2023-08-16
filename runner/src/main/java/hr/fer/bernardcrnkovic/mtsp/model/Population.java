package hr.fer.bernardcrnkovic.mtsp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class Population {
    private int id;
    private String label;
    private List<Solution> parettoFront;
    private List<Solution> individuals;

    @JsonIgnore private int problemId;
    @JsonIgnore private Problem problem;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Solution> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(List<Solution> individuals) {
        this.individuals = individuals;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonIgnore
    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problem_id) {
        this.problemId = problem_id;
    }

    @JsonIgnore
    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public int getSize() {
        return individuals.size();
    }

    public void setParettoFront(List<Solution> solutions) {
        parettoFront = solutions.stream().toList();
    }

    public List<Solution> getParettoFront() {
        return parettoFront;
    }
}
