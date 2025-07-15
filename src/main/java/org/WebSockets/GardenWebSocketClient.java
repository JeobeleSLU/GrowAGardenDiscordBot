package org.WebSockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.BaseClasses.Item;
import org.Bot.Obeserver;
import org.Bot.Parser;
import org.Bot.StockBot;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;

public class GardenWebSocketClient implements Runnable, Obeserver {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GardenWebSocketClient.class);
    public Parser parser;
    public StockBot bot;
    String uniqueIdentifier;
    private final int RECONNECT_DELAY_MS = 5000; // 5 seconds
    int numberOfIteration = 0;
    private final int debounceDelayMs = 10; // 1.5 second
    private Timer debounceTimer = new Timer();
    private final List<String> messageBuffer = new ArrayList<>();
    String url;
    boolean isEggTime = false;
    // 60 seconds in 1 minute, 30 minutes in alf an hour
    int secondsInHalfHour = 60 * 30;

    public GardenWebSocketClient() {
    }

    private void connect(String url) throws URISyntaxException {
        WebSocketClient client = new WebSocketClient(new URI(url)) {

            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("SocketThread:WebSocket connection established.");
            }
            /*
            TODO:Refactor to make this its own messaging extractor class
            Create a per user basis for the pinging for custom stocks
            Create a weather notification checker
            create a notification checker
            */
            @Override
            public synchronized void onMessage(String message) {
                numberOfIteration++;
                System.out.println("Number of messages incoming " + numberOfIteration);
                inspectMessage(message);

                if (message.isEmpty()) return;
                //If its egg time, set egg time to true
                if (isEggTime() ||isEggTime ){
                    isEggTime = true;
                    messageBuffer.add(message);
                    if (messageBuffer.size() < 3){
                        return;
                    }
                }else {
                    messageBuffer.add(message);
                }
                if (messageBuffer.size() < 2  && numberOfIteration > 1 ){
                    return;
                }

                debounceTimer.cancel();
                debounceTimer = new Timer();
                // Once the message is sent, set the egg time to false
                debounceTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String completeJson = appendJsons(messageBuffer);
                        ArrayList<Item> items = parser.parseMessage(completeJson);
                        if (items != null && !items.isEmpty()) {
                            bot.sendStock(items);
                            isEggTime = false;
                            messageBuffer.clear();
                        }
                    }
                }, debounceDelayMs); // run after 10 millisecond of inactivity
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                attemptReconnect();
                System.out.println("WebSocket connection closed.");
            }

            @Override
            public void onError(Exception ex) {
                attemptReconnect();
                System.err.println("WebSocket error: " + ex.getMessage());
            }
        };
        client.connect();
    }

    private boolean isEggTime() {
        //If it's divisible by 1800 or 30 mins its egg time
        return Instant.now().getEpochSecond() % secondsInHalfHour == 0;
    }

    private void inspectMessage(String message) {
        if(message.isEmpty()){
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(message);
            System.out.println("GWSThread: " + message);
            if (node.has("weather")){
                parser.getWeather(node);
                log.info("Weather happening ! ");
            }
            if (node.has("notification")){
                parser.getNotifications(node);
            }
        } catch (JsonProcessingException e) {
            //Do something here
        }
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
            String encodedUserId =UUID.randomUUID().toString();
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
        this.parser = new Parser(bot,bot);
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
