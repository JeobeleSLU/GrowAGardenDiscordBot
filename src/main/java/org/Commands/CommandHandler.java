package org.Commands;


import discord4j.core.object.entity.Message;
import org.Bot.GuildStorage;

import java.util.HashMap;

public class CommandHandler {
    private ICommand sendStockToChannel;
    private ICommand setChannel;
    private ICommand brodcastStock;
    private GuildStorage storage;



    HashMap<String,ICommand> commands;
    void initComponents(){
        this.commands = new HashMap<>();
        initCommands();
        initKeys();
        commands.put("setChannel",setChannel);
    }

    public CommandHandler() {
        initCommands();
    }

    private void initKeys() {
    }

    private void initCommands() {
        this.sendStockToChannel = new SendStockToChannel();
        this.setChannel = new SetChannel();
    }

    ICommand getCommand(String content){
        return commands.get(content);
    }
}
