package org.Commands;


import discord4j.core.object.entity.Message;
import org.Bot.GuildStorage;
import org.Utiilities.MessageBuilder;

import java.util.HashMap;
import java.util.Stack;

public class CommandHandler {
    private AbstractCommand sendStockToChannel;
    private AbstractCommand setChannel;
    private BrodcastToChannel brodcastStock;
    Stack<AbstractCommand> commandStack;
    MessageBuilder builder;

    HashMap<String,ICommand> commands;
    void initComponents(){
        this.commands = new HashMap<>();
        this.commandStack = new Stack<>();
        this.builder = new MessageBuilder(brodcastStock);
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
        commandStack.add(sendStockToChannel);
        this.setChannel = new SetChannel();
        commandStack.add(setChannel);
        this.brodcastStock = new BrodcastToChannel();
        commandStack.add(brodcastStock);

    }

   public ICommand getCommand(String content){
        return commands.get(content);
    }
}
