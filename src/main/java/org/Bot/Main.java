package org.Bot;


import org.WebSockets.GardenWebSocketClient;

public class Main {
    public static void main(String[] args) {
    GardenWebSocketClient client = new GardenWebSocketClient();
     StockBot bot = new StockBot(args[0],client);
     bot.run();
    }
}