package hr.fer.bernardcrnkovic.mtsp.rest;

import hr.fer.bernardcrnkovic.mtsp.model.Problem;

import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

interface ProblemRepo {

    @SqlQuery("""
    insert into problem(:p.label, :p.description, :p.costs)
    returning *
    """)
    Problem addProblem(@BindBean("p") Problem problem);
    @SqlQuery("""
    delete from problem where id = ? returning *
    """)
    Problem removeProblem(int id);
    @SqlQuery("select pop.id from problem as prob left join population as pop where prob.id = :id")
    List<Integer> getPopulationIds(int problemId);
    @SqlQuery("select * from problem")
    List<Problem> listProblems();
}
