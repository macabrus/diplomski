package hr.fer.bernardcrnkovic.mtsp;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.fer.bernardcrnkovic.mtsp.algo.NSGA2;
import hr.fer.bernardcrnkovic.mtsp.metric.FitnessMetricAccumulator;
import hr.fer.bernardcrnkovic.mtsp.model.Metrics;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Websocket connector setup
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
        BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        ObjectMapper mapper = new ObjectMapper();

        WebSocketClient ws = new WebSocketClient(client);
        var handler = new WsHandler(queue);
        List<Thread> workers = new ArrayList<>(Runtime.getRuntime().availableProcessors());

        /* Prepare evolution */
        NSGA2 nsga2 = new NSGA2(null, null);
        var fitnessAcc = new FitnessMetricAccumulator("fitness", new Metrics());
        fitnessAcc.addListener(queue::add);
        // attach listeners and notifiers
        nsga2.addIterationListener(fitnessAcc);
        nsga2.addStopNotifier(handler::shouldShutDown);

        /* Infinite reconnect loop (Until shutdown packet is received from controller) */
        while (ws.getOpenSessions().size() == 0) {
            System.out.println("Trying reconnect");
            ws.start();
            ws.connect(handler, URI.create("wss://127.0.0.1/api/stream"));

            // Request state for assigned task
            // int id = 1;
            // var res= client.GET(URI.create("https://127.0.0.1/api/run/" + id));
            /* This response => Run model */
            // TODO: extract state & config and pass to NSGA
            // while metrics are passed to metric handlers
            // queue contains messages produced from evolution thread
            // and is drained into websocket
            // var state = mapper.readValue(res.getContentAsString(), EvolutionState.class);
            workers.add(new Thread(nsga2::run));
            workers.get(workers.size() - 1).start();

            // wait for websocket closure
            handler.awaitClosure();
            System.out.println("Closure achieved");
            ws.stop();
            handler.resetLatch();
            break;
            // if (handler.shouldShutDown()) {
            //     // TODO: set notifier to true
            //     for (Thread worker : workers) {
            //         worker.join();
            //     }
            //     System.out.println("Received shutdown hint, exiting loop");
            //     ws.stop();
            //     break;
            // }
        }
    }
}
