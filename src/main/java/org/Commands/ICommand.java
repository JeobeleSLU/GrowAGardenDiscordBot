package org.Commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;

public interface ICommand {
    void execute(Message message, GatewayDiscordClient client);
}
