package hr.fer.bernardcrnkovic.mtsp.rest;

import hr.fer.bernardcrnkovic.mtsp.io.Loader;
import hr.fer.bernardcrnkovic.mtsp.model.Problem;
import hr.fer.bernardcrnkovic.mtsp.rest.model.CreateProblem;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
        var file = dto.file.get(0);
        String data = new String(Base64.getDecoder().decode(file.content), StandardCharsets.UTF_8);
        System.out.println(data);
        var problem = Loader.loadTSPLib(data);
        if (dto.label != null) {
            problem.setLabel(dto.label);
        }
        if (dto.description != null) {
            problem.setDescription(dto.description);
        }
        if (dto.color != null) {
            problem.setColor(dto.color);
        }
        return db.withExtension(ProblemRepo.class, repo -> repo.addProblem(problem));
    }

    public Problem removeProblem(Integer id) {
        return db.inTransaction(txn -> {
            var repo = txn.attach(ProblemRepo.class);
            var popIds = repo.getPopulationIds(id);
            if (!popIds.isEmpty()) {
                throw new RuntimeException("Populations depend on it!");
            }
            return repo.removeProblem(id);
        });
    }
}
