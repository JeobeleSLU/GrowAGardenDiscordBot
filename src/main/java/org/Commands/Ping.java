package org.Commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;

public class Ping extends AbstractCommand{
    @Override
    public void execute(Message message, GatewayDiscordClient client) {
        message.getChannel()
                .flatMap(channel -> channel.createMessage("Pong!"))
                .subscribe();   
    }
}
