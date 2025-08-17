package org.Commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import org.Bot.StockNotifier;

public class SetChannel extends AbstractCommand{
    StockNotifier notifier;
    @Override
    public void execute(Message message, GatewayDiscordClient client) {

    }
    void setStockNotifier(StockNotifier notifier){
        this.notifier = notifier;
    }
}
