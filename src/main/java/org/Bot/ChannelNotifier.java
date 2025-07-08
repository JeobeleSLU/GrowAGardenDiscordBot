package org.Bot;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Flux;

import java.util.*;

public class ChannelNotifier {
    HashMap<Snowflake, Snowflake> guildChannels;
    HashSet<Snowflake> guildList;

    public ChannelNotifier() {
        guildChannels = new HashMap<>();
         guildList= new HashSet<>();
    }
    void initComponents(HashMap<Snowflake, Snowflake> guildChannels, HashSet<Snowflake> guildList){
        this.guildChannels = guildChannels;
        this.guildList = guildList;

    };

    public void notifyStock(ArrayList<Item> items, GatewayDiscordClient client) {
        String stock = getStockMessage(items);
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
        String stock = getStockMessage(lastStock);
        if (stock.isEmpty()){

        }else {
        client.getChannelById(message.getChannelId())
                .ofType(MessageChannel.class)
                .flatMapMany(channel ->channel.createMessage(stock)
                )
                .subscribe();
        }
    }

    private String getStockMessage(ArrayList<Item> stock) {
        if (stock.isEmpty()){
            return "";
        }
        StringBuilder builder = new StringBuilder("```\n");

        Map<String, List<Item>> grouped = new LinkedHashMap<>();
        grouped.put("Seed Stock 🌱", new ArrayList<>());
        grouped.put("Gear Equipment Stock 🔧", new ArrayList<>());
        grouped.put("Egg Stock 🥚", new ArrayList<>());

        // Group items
        for (Item item : stock) {
            switch (item.getItemType()) {
                case "Gear" -> grouped.get("Gear Equipment Stock 🔧").add(item);
                case "Egg" -> grouped.get("Egg Stock 🥚").add(item);
                default -> grouped.get("Seed Stock 🌱").add(item);
            }
        }

        for (Map.Entry<String, List<Item>> entry : grouped.entrySet()) {
            if (entry.getValue().isEmpty()) continue;

            builder.append(entry.getKey()).append("\n");
            builder.append("Item                    |       Qty\n");
            builder.append("------------------------|-----\n");

            for (Item item : entry.getValue()) {
                String name = String.format("%-25s", item.getEmoji() + " " + item.displayName);
                String qty = String.format("%7s", item.quantity);
                builder.append(name).append("| ").append(qty).append("\n");
            }

            builder.append("\n");
        }
        builder.append("```");
        return builder.toString();
    }
    void refreshKeys(HashSet<Snowflake> keys){
        this.guildList = keys;
    }

    public void subscribeToEvent(HashMap<Snowflake, Snowflake> clientToAdd) {
        guildChannels.putAll(clientToAdd);
    }


    public void notifyBotOnline(GatewayDiscordClient client, ArrayList<Item> lastStock) {
        String stock = getStockMessage(lastStock);
        Flux.fromIterable(guildList)
                .flatMap(guildId -> {
                    Snowflake channelId = guildChannels.get(guildId);
                    return client.getChannelById(channelId)
                            .ofType(MessageChannel.class)
                            .flatMap(channel -> channel.createMessage("I am fully online sending stocks..... \n\n"+ stock));
                })
                .subscribe();
    }
}
