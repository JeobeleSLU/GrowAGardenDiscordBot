package org.Utiilities;

import discord4j.core.object.Embed;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.BaseClasses.Item;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class MessageBuilder {
    IStock botObeserver;

    public MessageBuilder(IStock botObeserver) {
        this.botObeserver = botObeserver;
    }

    public EmbedCreateSpec.Builder getStockMessage(ArrayList<Item> stock) {
        Map<String, StringBuilder> fieldBuilders = new LinkedHashMap<>();
        fieldBuilders.put("Seed Stock 🌱", new StringBuilder());
        fieldBuilders.put("Gear Equipment Stock 🔧", new StringBuilder());
        fieldBuilders.put("Egg Stock 🥚", new StringBuilder());
        fieldBuilders.put("Travelling Merchant Stock ✈️", new StringBuilder());

        boolean isMasterInStock = false;
        for (Item item : stock) {
            if ("Master Sprinkler".equals(item.getDisplayName())) {
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
                    .append(item.getDisplayName())
                    .append(" x").append(item.getQuantity()).append("\n");
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
        return embedBuilder;
    }

    public EmbedCreateSpec.Builder notifyMessage(String message) {
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                        .title("🛎️ Notification: ")
                .color(Color.GRAY_CHATEAU)
                .timestamp(Instant.now());
        embedBuilder.addField("Notification",message,true);
        if (message.equalsIgnoreCase("The Traveling Merchant has arrived")){
            botObeserver.setTravellingMerchant(true);
        }
            return embedBuilder;
    }
}
