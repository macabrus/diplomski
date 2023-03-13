package hr.fer.bernardcrnkovic.mtsp;

import hr.fer.bernardcrnkovic.mtsp.model.Problem;
import hr.fer.bernardcrnkovic.mtsp.rest.Api;
import io.javalin.Javalin;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Server setup
 *
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        Javalin javalin = Javalin.create((config) -> {
            config.plugins.register((app) -> {
                var jdbi = Jdbi.create("jdbc:sqlite:app.db?journal_mode=WAL");
                jdbi.installPlugin(new SqlObjectPlugin());
                jdbi.registerRowMapper(BeanMapper.factory(Problem.class));
                app.attribute("db", jdbi);
                app.before(ctx -> {
                    ctx.attribute("db", jdbi);
                });
            });
        });
        var api = new Api();
        javalin.post("/problem/new", api::addProblem);
        javalin.delete("/problem/{id}", api::removeProblem);
        javalin.events(e -> {
            e.serverStarting(() -> {
                Jdbi db = javalin.attribute("db");
                db.useTransaction(txn -> {
                    txn.createScript(
                        Files.readString(
                            Paths.get(
                                App.class.getClassLoader()
                                .getResource("schema.sql")
                                .toURI()
                            )
                        )
                    ).execute();
                });
            });
            e.serverStopped(() -> {
                Jdbi db = javalin.attribute("db");
                db.useTransaction(txn -> {
                    txn.createScript(
                        Files.readString(
                            Paths.get(
                                App.class.getClassLoader()
                                .getResource("drop.sql")
                                .toURI()
                            )
                        )
                    ).execute();
                });
            });
        });
        javalin.start(8080);
    }
}
