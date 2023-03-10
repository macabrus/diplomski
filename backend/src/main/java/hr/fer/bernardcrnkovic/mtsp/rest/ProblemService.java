package hr.fer.bernardcrnkovic.mtsp.rest;

import hr.fer.bernardcrnkovic.mtsp.model.Problem;
import hr.fer.bernardcrnkovic.mtsp.rest.model.CreateProblem;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class ProblemService {

    private Jdbi db;

    public ProblemService(Jdbi db) {
        this.db = db;
    }

    public List<Problem> listProblems() {
        return db.withExtension(ProblemRepo.class, ProblemRepo::listProblems);
    }

    public Problem addProblem(CreateProblem dto) {
        dto.
        return db.withExtension(ProblemRepo.class, repo -> repo.addProblem(prob));
    }
}
