package org.Bot;

import discord4j.core.object.entity.Message;

public interface StockNotifier {
    public void notifyChannel(Message message);
}
