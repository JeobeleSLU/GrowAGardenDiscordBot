package org.Bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.BaseClasses.Item;
import org.BaseClasses.Weather;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Stack;

public class Parser {
    ObjectMapper objectMapper;
    JsonNode json;
    final String[] ARRAY_OF_ITEMS = {"seed_stock","gear_stock","egg_stock","travelingmerchant_stock"};
    final String[] ARRAY_OF_ITEM_TYPES = {"Seed","Gear","Egg","Travelling Merchant"};
    final String[] WEATHER_NODES = {"start_duration_unix","end_duration_unix","duration","weather_id","weather_name"};
    final String[] EMOJIS = {"🌱", "🛠️", "🥚","✈️"};
    NotificationHandler bot;
    WeatherAlert weather;
    String previousWeather = "";
    public Parser(NotificationHandler bot,WeatherAlert weather) {
        init();
        this.bot = bot;
        this.weather = weather;
    }
    public void init(){
        this.objectMapper = new ObjectMapper();
    }
    public ArrayList<Item> parseMessage(String message){
        try {
            this.json = objectMapper.readTree(message);
            return printMessage();
        } catch (JsonProcessingException e) {
            System.out.println("SocketThread:cant read tree");
            return null;
        }
    }

    public void getWeather(JsonNode json) {
        if(!json.has("weather")){
            return;
        }
            JsonNode weatherArray = json.get("weather");
        if (isValidWeatherArray(weatherArray)) return;

        Stack<Weather> activeWeather = checkForActiveWeathers(weatherArray);
        if (!activeWeather.isEmpty()) {
                    weather.nottifyWeather(activeWeather);
            }
    }

    private boolean isValidWeatherArray(JsonNode weatherArray) {
        return weatherArray == null && !weatherArray.isArray() && weatherArray.isEmpty();
    }

    private Stack<Weather> checkForActiveWeathers(JsonNode weathers) {
        Stack<Weather> weatherStack = new Stack<>();
        for (int i = 0 ; i < weathers.size();i++){
            JsonNode node = weathers.get(i);
            if (node.get("active").asBoolean()){
                Weather tempWeather = extractWeather(node);
                weatherStack.add(tempWeather);
                System.out.println("An active weather is occuring");
            }
        }
        return weatherStack;
    }

    //Extract weather as a weather object instead of the weather string
    private Weather extractWeather(JsonNode node) {
        ArrayList<String> attributes = new ArrayList<>();
        for (int i = 0; i < WEATHER_NODES.length;i++){
            attributes.add(node.get(WEATHER_NODES[i]).asText());
        }
        if (attributes.size() != 5 ){
            return null;
        }
        long start = Long.parseLong(attributes.get(0));
        long end = Long.parseLong(attributes.get(1));
        long duration = Long.parseLong(attributes.get(2));
        String weatherID = attributes.get(3);
        String weatherName = attributes.get(4);
        return new Weather(start,end,duration,weatherID,weatherName);
    }
    private boolean validateTime(long start, long end) {
        long currentTime = Instant.now().getEpochSecond();
        return currentTime >= start && currentTime < end;
    }

    public void getNotifications(JsonNode json) {
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
        ArrayList<Item> items = new ArrayList<>();

        for (int i = 0; i < ARRAY_OF_ITEMS.length; i++) {
            String stockKey = ARRAY_OF_ITEMS[i];
            String emoji = EMOJIS[i];
            String equipmentType = ARRAY_OF_ITEM_TYPES[i];

            if (!json.has(stockKey)) continue;

            JsonNode stockNode = json.get(stockKey);

            // Special case: travelingmerchant_stock is an object with a "stock" array inside
            if (stockKey.equals("travelingmerchant_stock") && stockNode.has("stock")) {
                JsonNode merchantArray = stockNode.get("stock");
                if (merchantArray.isArray()) {
                    for (JsonNode item : merchantArray) {
                        addItemIfValid(item, emoji, equipmentType, items);
                    }
                }
            } else if (stockNode.isArray()) {
                for (JsonNode item : stockNode) {
                    addItemIfValid(item, emoji, equipmentType, items);
                }
            }
        }

        return items;
    }
    boolean isValidItem(JsonNode item) {
        return item.hasNonNull("item_id")
                && item.hasNonNull("display_name")
                && item.hasNonNull("quantity");
    }
    private void addItemIfValid(JsonNode item, String emoji, String type, ArrayList<Item> items) {
        if (!isValidItem(item)) {
            System.out.println(" Skipping malformed item: " + item.toPrettyString());
            return;
        }

        String itemId = item.get("item_id").asText();
        String displayName = item.get("display_name").asText();
        int quantity = item.get("quantity").asInt();

        items.add(new Item(itemId, displayName, String.valueOf(quantity), emoji, type));

        System.out.printf("✅ Item: %s (%s), Quantity: %d, Icon: %s%n",
                displayName, itemId, quantity, emoji);
    }

}

