package hr.fer.bernardcrnkovic.mtsp;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.fer.bernardcrnkovic.mtsp.algo.FitnessPublisher;
import hr.fer.bernardcrnkovic.mtsp.algo.NSGA2;
import hr.fer.bernardcrnkovic.mtsp.model.EvolutionState;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Server setup
 */
public class App {
    public static void main(String[] args) throws Exception {

        /* Runner implementation to receive EvolutionState and execute it until paused */
        SslContextFactory.Client sec = new SslContextFactory.Client();
        sec.setTrustAll(true);

        /* https://stackoverflow.com/a/67176246 */
        ClientConnector clientConnector = new ClientConnector();
        clientConnector.setSslContextFactory(sec);

        /* https://github.com/jetty-project/embedded-jetty-websocket-examples/blob/10.0.x/native-jetty-websocket-example/src/main/java/org/eclipse/jetty/demo/EventClient.java */
        HttpClient client = new HttpClient(new HttpClientTransportDynamic(clientConnector));
        Queue<Object> queue = new ConcurrentLinkedQueue<>();
        ObjectMapper mapper = new ObjectMapper();

        WebSocketClient ws = new WebSocketClient(client);
        List<Thread> workers = new ArrayList<>(Runtime.getRuntime().availableProcessors());
        /* Infinite reconnect loop (Until shutdown packet is received from controller) */
        while (ws.getOpenSessions().size() == 0) {
            System.out.println("Trying reconnect");
            ws.start();
            var handler = new WsHandler(queue);
            ws.connect(handler, URI.create("wss://127.0.0.1/api/stream"));

            // Request state for assigned task
            int id = 1;
            var res= client.GET(URI.create("https://127.0.0.1/api/run/" + id));
            var state = mapper.readValue(res.getContentAsString(), EvolutionState.class);

            /* Prepare evolution */
            NSGA2 nsga2 = new NSGA2(state);
            // attach listeners and notifiers
            nsga2.addIterationListener(new FitnessPublisher(queue));
            nsga2.addStopNotifier(handler::shouldShutDown);
            workers.add(new Thread(nsga2::run));

            // wait for websocket closure
            handler.awaitClosure();
            if (handler.shouldShutDown()) {
                // TODO: set notifier to true
                for (Thread worker : workers) {
                    worker.join();
                }
                System.out.println("Received shutdown hint, exiting loop");
                ws.stop();
                break;
            }
        }
    }
}
