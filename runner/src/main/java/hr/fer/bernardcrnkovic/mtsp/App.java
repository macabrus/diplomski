package hr.fer.bernardcrnkovic.mtsp;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import jakarta.websocket.ClientEndPoint;

/**
 * Server setup
 *
 */
public class App {
    public static void main(String[] args) {
        /* Runner implementation to receive EvolutionState and execute it until paused */
        WebSocketClient client = new WebSocketClient();
        // Use a standard, HTTP/1.1, HttpClient.
        HttpClient httpClient = new HttpClient();

        // Create and start WebSocketClient.
        WebSocketClient webSocketClient = new WebSocketClient(httpClient);
        webSocketClient.start();

        // The client-side WebSocket EndPoint that
        // receives WebSocket messages from the server.
        ClientEndPoint clientEndPoint = new ClientEndPoint();
        // The server URI to connect to.
        URI serverURI = URI.create("wss://127.0.0.1/api/stream");

        // Connect the client EndPoint to the server.
        CompletableFuture<Session> clientSessionPromise = webSocketClient.connect(clientEndPoint, serverURI);
    }
    // public static void main(String[] args) {
    //     System.out.println("Hello World!");
    //     Javalin javalin = Javalin.create((config) -> {
    //         config.plugins.register((app) -> {
    //             var jdbi = Jdbi.create("jdbc:sqlite:app.db?journal_mode=WAL");
    //             jdbi.installPlugin(new SqlObjectPlugin());
    //             jdbi.registerRowMapper(BeanMapper.factory(Problem.class));
    //             app.attribute("db", jdbi);
    //             app.before(ctx -> {
    //                 ctx.attribute("db", jdbi);
    //             });
    //         });
    //     });
    //     var api = new Api();
    //     javalin.post("/problem/new", api::addProblem);
    //     javalin.delete("/problem/{id}", api::removeProblem);
    //     javalin.events(e -> {
    //         e.serverStarting(() -> {
    //             Jdbi db = javalin.attribute("db");
    //             db.useTransaction(txn -> {
    //                 txn.createScript(
    //                     Files.readString(
    //                         Paths.get(
    //                             App.class.getClassLoader()
    //                             .getResource("schema.sql")
    //                             .toURI()
    //                         )
    //                     )
    //                 ).execute();
    //             });
    //         });
    //         e.serverStopped(() -> {
    //             Jdbi db = javalin.attribute("db");
    //             db.useTransaction(txn -> {
    //                 txn.createScript(
    //                     Files.readString(
    //                         Paths.get(
    //                             App.class.getClassLoader()
    //                             .getResource("drop.sql")
    //                             .toURI()
    //                         )
    //                     )
    //                 ).execute();
    //             });
    //         });
    //     });
    //     javalin.start(8080);
    // }
}
