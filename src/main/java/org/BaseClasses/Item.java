package org.BaseClasses;

public class Item {
    String displayName,quantity,emoji,itemId,itemType;

    public Item(String itemId,String displayName, String quantity, String emoji,String itemType) {
        this.displayName = displayName;
        this.quantity = quantity;
        this.emoji = emoji;
        this.itemId = itemId;
        this.itemType = itemType;
    }
    public String getItemType() {
        return itemType;
    }

    public String getItemId() {
        return itemId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }


    public String getEmoji() {
        return emoji;
    }
}
