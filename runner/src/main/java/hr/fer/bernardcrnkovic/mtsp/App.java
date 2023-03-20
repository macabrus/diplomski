package hr.fer.bernardcrnkovic.mtsp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.Map;

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
        //client.start();
        WebSocketClient ws = new WebSocketClient(client);
        ws.start();

        Session session = ws.connect(new WebSocketAdapter(), URI.create("wss://127.0.0.1/api/stream")).get();

        ObjectMapper mapper = new ObjectMapper();

        /* Register as Worker */
        // session.getRemote().sendString("{\"type\": \"Hello!\"} ");
        session.getRemote().sendString(mapper.writeValueAsString(Map.of(
            "type", "runner",
            "action", "register",
            "slots", Runtime.getRuntime().availableProcessors()
        )));
        Thread.sleep(10_000);

        ws.stop();
    }
}
