package org.WebSockets;

import org.Bot.Obeserver;
import org.Bot.Parser;
import org.Bot.StockBot;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

public class GardenWebSocketClient implements Runnable, Obeserver {
    public Parser parser;
    public StockBot bot;
    String uniqueIdentifier ;

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
            public void onMessage(String message) {
                if (!message.isEmpty()){
                    System.out.println(message);
                    bot.sendStock(parser.parseMessage(message));
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("WebSocket connection closed.");
            }

            @Override
            public void onError(Exception ex) {
                System.err.println("WebSocket error: " + ex.getMessage());
            }
        };
        client.connect();
    }

    @Override
    public void run() {
        try {
            String userId = uniqueIdentifier;
            String encodedUserId = URLEncoder.encode(userId, StandardCharsets.UTF_8);
            String url = "wss://websocket.joshlei.com/growagarden?user_id=" + encodedUserId;
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
}
