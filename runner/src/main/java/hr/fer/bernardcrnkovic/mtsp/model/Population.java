package hr.fer.bernardcrnkovic.mtsp.model;

import java.util.List;

public class Population {
    private String label;
    private List<Solution> individuals;
    private int id;
    private int problemId;
    private Problem problem;

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

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problem_id) {
        this.problemId = problem_id;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public int getSize() {
        return individuals.size();
    }
}
