package hr.fer.bernardcrnkovic.mtsp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hr.fer.bernardcrnkovic.mtsp.model.DataPoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class WsHandler extends WebSocketAdapter {

    private CountDownLatch closureLatch = new CountDownLatch(1);
    private final ObjectMapper mapper = new ObjectMapper();
    private final Random random = new Random();
    private final boolean shouldShutDown = false;
    private BlockingQueue<Object> queue;

    {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public WsHandler(BlockingQueue<Object> queue) {
        this.queue = queue;
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        /* Register as Worker after connection */
        try {
            getRemote().sendString(mapper.writeValueAsString(Map.of(
                "type", "runner",
                "action", "register",
                "slots", Runtime.getRuntime().availableProcessors()
            )));
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (closureLatch.getCount() != 0) {
            try {
                var dp = queue.take();
                if (!(dp instanceof DataPoint)) {
                    System.out.println("Received non-datapoint object on queue. Assuming sentinel, exiting...");
                    break;
                }
                // Take datapoints from queue forever
                getRemote().sendString(mapper.writeValueAsString(Map.of(
                    "type", "update",
                    "run_id", -1,
                    "metric", "fitness",
                    "value", dp
                )));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        System.out.println("received: " + message);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        closureLatch.countDown();
    }

    public void awaitClosure() throws InterruptedException {
        closureLatch.await();
    }

    public boolean shouldShutDown() {
        return shouldShutDown;
    }

    public void resetLatch() {
        closureLatch = new CountDownLatch(1);
    }
}
