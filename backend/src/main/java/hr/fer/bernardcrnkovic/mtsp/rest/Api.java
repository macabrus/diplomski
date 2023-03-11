package hr.fer.bernardcrnkovic.mtsp.rest;


import hr.fer.bernardcrnkovic.mtsp.rest.model.CreateProblem;
import io.javalin.http.Context;

public class Api {
    public void addProblem(Context ctx) {
        var dto = ctx.bodyAsClass(CreateProblem.class);
        ctx.json(problemService(ctx).addProblem(dto));
    }

    public void removeProblem(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        problemService(ctx);
        System.out.println(id + " deleted!");
    }

    public void listProblems(Context ctx) {
        ctx.json(problemService(ctx).listProblems());
    }

    public void createPopulation(Context ctx) {

    }

    public void removePopulation(Context ctx) {

    }

    public void removeRuns(Context ctx) {
    }

    /**
     * Creates new population from existing run
     * @param ctx
     */
    public void createPopulationFromRun(Context ctx) {

    }

    private ProblemService problemService(Context ctx) {
        return new ProblemService(ctx.attribute("db"));
    }
}
