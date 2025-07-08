package org.Bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;

public class Parser {
    ObjectMapper objectMapper;
    JsonNode json;
    final String[] ARRAY_OF_ITEMS = {"seed_stock","gear_stock","egg_stock","travelingmerchant_stock"};
    final String[] ARRAY_OF_ITEM_TYPES = {"Seed","Gear","Egg","Travelling Merchant"};
    final String[] EMOJIS = {"🌱", "🛠️", "🥚","✈️"};
    NotificationHandler bot;
    String previousWeather = "";
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
            if (json.has("notification")){
                getNotifications(message);
            }
            if (json.has("weather")){
                getWeather(message);
            }
            return printMessage();
        } catch (JsonProcessingException e) {
            System.out.println("SocketThread:cant read tree");
            return null;
        }
    }

    private void getWeather(String message) {
        JsonNode weatherArray = json.get("weather");
        if (weatherArray!= null && weatherArray.isArray() && weatherArray.size() > 0){
            JsonNode weathers = weatherArray;
           ArrayList<String> activeWeather = checkForActiveWeathers(weathers);
           if (!weathers.isEmpty()){
               createMessageForWeather(activeWeather);
           }
        }
    }

    private void createMessageForWeather(ArrayList<String> activeWeather) {
        if (activeWeather.size() == 1){
            bot.triggerEventNotification("Current weather is "+activeWeather.get(0));
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Current Weather are ");
        builder.append("[");
        activeWeather.forEach(e-> {
            builder.append(e).append(" + ");
        });
        builder.append("]");
        }

    private ArrayList<String> checkForActiveWeathers(JsonNode weathers) {
        ArrayList<String> activeWeathers = new ArrayList<>();
        for (int i = 0 ; i < weathers.size();i++){
            JsonNode node = weathers.get(i);
            if (node.get("active").asBoolean()){
                System.out.println("An active weather is occuring");
                activeWeathers.add(node.get("weather_name").asText());
            }
        }
        return activeWeathers;
    }

    private boolean validateTime(long start, long end) {
        long currentTime = Instant.now().getEpochSecond();
        return currentTime >= start && currentTime < end;
    }

    private void getNotifications(String message) {
        long currentTime = Instant.now().getEpochSecond();
        JsonNode notifArray = json.get("notification");
        if (notifArray != null && notifArray.isArray() && notifArray.size() > 0) {
            JsonNode notif = notifArray.get(0);

            JsonNode messageNode = notif.get("message");
            if (messageNode != null && !messageNode.isNull()) {
                JsonNode start = notif.get("timestamp");
                //display only if 20 seconds has elapsed from the message
                if ( validateTime(start.asLong(),currentTime+ 20) ){
                    bot.triggerEventNotification(messageNode.asText());
                }
            }
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

