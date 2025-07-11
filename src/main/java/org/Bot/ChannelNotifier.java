package org.Bot;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Flux;

import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.*;

public class ChannelNotifier {
    HashMap<Snowflake, Snowflake> guildChannels;
    HashSet<Snowflake> guildList;
    static int cycleCounter = 1;

    public ChannelNotifier() {
        guildChannels = new HashMap<>();
         guildList= new HashSet<>();
    }
    void initComponents(HashMap<Snowflake, Snowflake> guildChannels, HashSet<Snowflake> guildList){
        this.guildChannels = guildChannels;
        this.guildList = guildList;

    };

    public void notifyStock(ArrayList<Item> items, GatewayDiscordClient client) {
        EmbedCreateSpec stock = getStockMessage(items);
        Flux.fromIterable(guildList)
                .flatMap(guildId -> {
                    Snowflake channelId = guildChannels.get(guildId);
                    return client.getChannelById(channelId)
                            .ofType(MessageChannel.class)
                            .flatMap(channel -> channel.createMessage(stock));
                })
                .subscribe();
    }
    public void notifyChannel(Message message, ArrayList<Item> lastStock, GatewayDiscordClient client) {
        if (lastStock == null || lastStock.isEmpty()) {
            client.getChannelById(message.getChannelId())
                    .ofType(MessageChannel.class)
                    .flatMap(channel -> channel.createMessage("Can't get previous stock."))
                    .subscribe();
            return;
        }
        EmbedCreateSpec stock = getStockMessage(lastStock);
        client.getChannelById(message.getChannelId())
                .ofType(MessageChannel.class)
                .flatMapMany(channel ->channel.createMessage(stock)
                )
                .subscribe();
        }

    private EmbedCreateSpec getStockMessage(ArrayList<Item> stock) {
        Map<String, StringBuilder> fieldBuilders = new LinkedHashMap<>();
        fieldBuilders.put("Seed Stock 🌱", new StringBuilder());
        fieldBuilders.put("Gear Equipment Stock 🔧", new StringBuilder());
        fieldBuilders.put("Egg Stock 🥚", new StringBuilder());
        fieldBuilders.put("Travelling Merchant Stock ✈️", new StringBuilder());

        boolean isMasterInStock = false;

        for (Item item : stock) {
            if ("Master Sprinkler".equals(item.displayName)) {
                isMasterInStock = true;
            }

            String itemType = item.getItemType();
            String fieldKey = switch (itemType) {
                case "Gear" -> "Gear Equipment Stock 🔧";
                case "Egg" -> "Egg Stock 🥚";
                case "Travelling Merchant" -> "Travelling Merchant Stock ✈️";
                default -> "Seed Stock 🌱";
            };

            StringBuilder field = fieldBuilders.get(fieldKey);
            field.append(item.getEmoji()).append(" ")
                    .append(item.displayName)
                    .append(" x").append(item.quantity).append("\n");
        }

        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .title("📦 Stock Report")
                .color(Color.GRAY_CHATEAU)
                .timestamp(Instant.now());

        // Add only non-empty stock fields as inline
        for (Map.Entry<String, StringBuilder> entry : fieldBuilders.entrySet()) {
            String value = entry.getValue().toString();
            if (entry.getValue().isEmpty()){
                continue;
            }
            embedBuilder.addField(entry.getKey(), " " + value ,true);
        }
        // Alert field (non-inline) if Master Sprinkler is found
        if (isMasterInStock) {
            embedBuilder.addField("🔥 Alert", "@everyone Master Sprinkler is in stock!", false);
        }
        return embedBuilder.build();
    }
    void refreshKeys(HashSet<Snowflake> keys){
        this.guildList = keys;
    }

    public void subscribeToEvent(HashMap<Snowflake, Snowflake> clientToAdd) {
        guildChannels.putAll(clientToAdd);
    }

    public void notifyBotOnline(GatewayDiscordClient client) {
        Flux.fromIterable(guildList)
                .flatMap(guildId -> {
                    Snowflake channelId = guildChannels.get(guildId);
                    return client.getChannelById(channelId)
                            .ofType(MessageChannel.class)
                            .flatMap(channel -> channel.createMessage("```I am fully online sending stocks..... 🌱```\n\n"));
                })
                .subscribe();
    }

    public void notifyMessage(String message, GatewayDiscordClient client) {
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .title("🛎️ Notification: ")
                .color(Color.GRAY_CHATEAU)
                .timestamp(Instant.now());

        embedBuilder.addField("Notification",message,true);
        System.out.println("Cycle: " + cycleCounter);
        cycleCounter++;
        Flux.fromIterable(guildList)
                .flatMap(guildId -> {
                    Snowflake channelId = guildChannels.get(guildId);
                    return client.getChannelById(channelId)
                            .ofType(MessageChannel.class)
                            .flatMap(channel -> channel.createEmbed(embedBuilder.build()));
                })
                .subscribe();
    }

    public void sendToDevConsoles(Exception ex, GatewayDiscordClient gateway) {

    }

    public void alertWeather(ArrayList<String> weathers, GatewayDiscordClient client) {
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .title("☁️ Weather : ")
                .color(Color.GRAY_CHATEAU)
                .timestamp(Instant.now());
        if (weathers.size() == 1){
            embedBuilder.addField("Current Weather: ", weathers.get(0),true);
        }else {
            String extractedString = extractWeathers(weathers);
            embedBuilder.addField("Current Weather: ", extractedString, true);
        }
        Flux.fromIterable(guildList)
                .flatMap(guildId -> {
                    Snowflake channelId = guildChannels.get(guildId);
                    return client.getChannelById(channelId)
                            .ofType(MessageChannel.class)
                            .flatMap(channel -> channel.createEmbed(embedBuilder.build()));
                })
                .subscribe();
    }

    private String extractWeathers(ArrayList<String> weathers) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0 ; i < weathers.size(); i++){

            if (i == weathers.size() -1 ){
                builder.append("]");
                break;
            }
            builder.append(weathers.get(i));
            if (i != weathers.size()- 2){
                builder.append(" + ");
            }
        }
        return builder.toString();
    }
}
