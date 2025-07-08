package org.Bot;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.awt.*;

public class Item {
    String displayName,quantity,emoji,itemId,itemType;

    public Item(String itemId,String displayName, String quantity, String emoji,String itemType) {
        this.displayName = displayName;
        this.quantity = quantity;
        this.emoji = emoji;
        this.itemId = itemId;
        this.itemType = itemType;
    }
    public EmbedCreateSpec getEmebed() {
        return EmbedCreateSpec.builder()
                .title(this.displayName + " (" + this.itemId + ")")
                .thumbnail(this.emoji)
                .addField("Quantity", String.valueOf(this.quantity), false)
                .color(Color.ORANGE)
                .build();
    }

    public String getItemType() {
        return itemType;
    }

    public String getEmoji() {
        return emoji;
    }
}
