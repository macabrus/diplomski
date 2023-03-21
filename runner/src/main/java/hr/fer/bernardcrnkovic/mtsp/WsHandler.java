package hr.fer.bernardcrnkovic.mtsp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class WsHandler extends WebSocketAdapter {

    private final CountDownLatch closureLatch = new CountDownLatch(1);
    private final ObjectMapper mapper = new ObjectMapper();
    private final Random random = new Random();
    private final boolean shouldShutDown = false;
    private Queue<Object> queue;

    public WsHandler(Queue<Object> queue) {
        this.queue = queue;
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        /* Register as Worker */
        try {
            getRemote().sendString(mapper.writeValueAsString(Map.of(
                "type", "runner",
                "action", "register",
                "slots", Runtime.getRuntime().availableProcessors()
            )));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int i = 0;
        while (closureLatch.getCount() != 0) {
            // while(isNotConnected() && closureLatch.getCount() != 0) {
            // }
            try {
                getRemote().sendString(mapper.writeValueAsString(Map.of(
                    "type", "update",
                    "run_id", -1,
                    "index", i++,
                    "value", random.nextInt(0, 100)
                )));
                getRemote().sendPong(ByteBuffer.allocate(1));
                Thread.sleep(1000);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
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
}
