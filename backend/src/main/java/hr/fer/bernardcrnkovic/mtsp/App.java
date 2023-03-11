package hr.fer.bernardcrnkovic.mtsp;

import hr.fer.bernardcrnkovic.mtsp.rest.Api;
import io.javalin.Javalin;
import org.jdbi.v3.core.Jdbi;

import java.nio.charset.StandardCharsets;
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
                app.attribute("db", Jdbi.create("jdbc:sqlite:app.db?journal_mode=WAL"));
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
                        new Scanner(App.class.getResourceAsStream("/schema.sql"), StandardCharsets.UTF_8)
                            .useDelimiter("\\A")
                            .next()
                    ).execute();
                });
            });
            e.serverStopped(() -> {
                Jdbi db = javalin.attribute("db");
                db.useTransaction(txn -> {
                    txn.createScript(
                        new Scanner(App.class.getResourceAsStream("/drop.sql"), StandardCharsets.UTF_8)
                            .useDelimiter("\\A")
                            .next()
                    ).execute();
                });
            });
        });
        javalin.start(8080);
    }
}
