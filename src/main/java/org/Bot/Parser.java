package org.Bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

public class Parser {
    ObjectMapper objectMapper;
    JsonNode json;
    final String[] ARRAY_OF_ITEMS = {"seed_stock","gear_stock","egg_stock,","travelingmerchant_stock"};
    final String[] ARRAY_OF_ITEM_TYPES = {"Seed","Gear","Egg","Traveling merchant"};
    final String[] EMOJIS = {"🌱", "🛠️", "🥚","✈️"};
    NotificationHandler bot;
    public Parser(NotificationHandler bot) {
        init();
        this.bot = bot;
    }
    public void init(){
        this.objectMapper = new ObjectMapper();
    }
    public ArrayList<Item> parseMessage(String message){
        try {
            this.json = objectMapper.readTree(message);
            JsonNode notifArray = json.get("notification");
            if (notifArray != null && notifArray.isArray() && notifArray.size() > 0) {
                JsonNode notif = notifArray.get(0);
                JsonNode messageNode = notif.get("message");
                if (messageNode != null && !messageNode.isNull()) {
                    bot.triggerEventNotification(messageNode.asText());
                }
            }
            return printMessage();
        } catch (JsonProcessingException e) {
            System.out.println("SocketThread:cant read tree");
            return null;
        }
    }

    private ArrayList<Item> printMessage() {


        StringBuilder sb = new StringBuilder();
        ArrayList<Item> items = new ArrayList<>();
        for (int i = 0; i < ARRAY_OF_ITEMS.length; i++){
            String emoji = EMOJIS[i];
           String equipmentType = ARRAY_OF_ITEM_TYPES[i];
        if (json.has(ARRAY_OF_ITEMS[i])) {
                JsonNode seedStock = json.get(ARRAY_OF_ITEMS[i]);
                sb.append("\n--- SocketThread:Seed Stock ---\n");
                for (JsonNode item : seedStock) {
                    String itemId = item.get("item_id").asText();
                    String displayName = item.get("display_name").asText();
                    int quantity = item.get("quantity").asInt();
                    items.add(new Item(itemId,displayName,String.valueOf(quantity),emoji,equipmentType));
                    sb.append(String.format("Item: %s (%s), Quantity: %d, Icon: %s%n",
                            displayName, itemId, quantity,emoji));
                    System.out.println(sb.toString());
                }
            }
        }
        return items;
    }

}

