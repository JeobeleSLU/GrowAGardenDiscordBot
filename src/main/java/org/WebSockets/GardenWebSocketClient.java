package org.WebSockets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.Bot.Item;
import org.Bot.Obeserver;
import org.Bot.Parser;
import org.Bot.StockBot;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class GardenWebSocketClient implements Runnable, Obeserver {
    public Parser parser;
    public StockBot bot;
    String uniqueIdentifier;
    private final int RECONNECT_DELAY_MS = 5000; // 5 seconds
    int numberOfIteration = 0;
    private final int debounceDelayMs = 10; // 1.5 second
    private Timer debounceTimer = new Timer();
    private final List<String> messageBuffer = new ArrayList<>();
    String url;

    public GardenWebSocketClient(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    private void connect(String url) throws URISyntaxException {
        WebSocketClient client = new WebSocketClient(new URI(url)) {

            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("SocketThread:WebSocket connection established.");
            }

            @Override
            public synchronized void onMessage(String message) {
                numberOfIteration++;
                System.out.println("Number of messages incoming " + numberOfIteration);

                if (message.isEmpty()) return;

                messageBuffer.add(message);
                if (messageBuffer.size() < 2  && numberOfIteration > 1 ){
                    return;
                }

                debounceTimer.cancel();
                debounceTimer = new Timer();

                debounceTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String completeJson = appendJsons(messageBuffer);
                        ArrayList<Item> items = parser.parseMessage(completeJson);
                        if (items != null && !items.isEmpty()) {
                            bot.sendStock(items);
                            messageBuffer.clear();
                        }
                    }
                }, debounceDelayMs); // run after 2 second of inactivity
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                reconnect();
                System.out.println("WebSocket connection closed.");
            }

            @Override
            public void onError(Exception ex) {
                reconnect();
                System.err.println("WebSocket error: " + ex.getMessage());
            }
        };
        client.connect();
    }

    private String appendJsons(List<String> messageBuffer) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode combined = mapper.createObjectNode();

        for (String json : messageBuffer) {
            try {
                JsonNode node = mapper.readTree(json);
                if (node.isObject()) {
                    node.fields().forEachRemaining(entry -> combined.set(entry.getKey(), entry.getValue()));
                }
            } catch (Exception e) {
                System.err.println("Invalid JSON during merge: " + e.getMessage());
            }
        }

        try {
            return mapper.writeValueAsString(combined);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }


    @Override
    public void run() {
        try {
            String userId = uniqueIdentifier;
            String encodedUserId = URLEncoder.encode(userId, StandardCharsets.UTF_8);
            url = "wss://websocket.joshlei.com/growagarden?user_id=" + encodedUserId;
            connect(url);
            System.out.println("Connected successfully to the web socket");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initCon(StockBot bot) {
        this.bot = bot;
        this.parser = new Parser(bot);
        run();
    }

    private void attemptReconnect() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("Attempting to reconnect...");
                    connect(url); // Store currentUrl as a field
                } catch (Exception e) {
                    System.err.println("Reconnect failed: " + e.getMessage());
                    attemptReconnect(); // Try again
                }
            }
        }, RECONNECT_DELAY_MS);
    }
}
