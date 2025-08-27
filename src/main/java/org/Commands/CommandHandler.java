package org.Commands;


import org.Utiilities.MessageBuilder;

import java.util.HashMap;
import java.util.Stack;

public class CommandHandler {
    private AbstractCommand sendStockToChannel;
    private AbstractCommand setChannel;
    private AbstractCommand ping;


    Stack<AbstractCommand> commandStack;
    MessageBuilder builder;

    HashMap<String,ICommand> commands;
    public void initComponents(){
        this.commands = new HashMap<>();
        this.commandStack = new Stack<>();
        this.builder = new MessageBuilder();
        initCommands();
        initKeys();
    }

    public CommandHandler() {
        initCommands();
    }

    private void initKeys() {
        commands.put("setChannel",setChannel);
        commands.put("ping",ping);
        commands.put("sendStocks",sendStockToChannel);
    }

    private void initCommands() {
        this.commandStack = new Stack<>();
        this.sendStockToChannel = new SendStockToChannel();
        commandStack.add(sendStockToChannel);
        this.setChannel = new SetChannel();
        commandStack.add(setChannel);
        this.ping = new Ping();
        commandStack.add(ping);
        initMessage();
    }

    private void initMessage() {
        while (!commandStack.isEmpty()){
            commandStack.pop().setBuilder(builder);
        }
    }

    public AbstractCommand getSendStockToChannel() {
        return sendStockToChannel;
    }

    public AbstractCommand getSetChannel() {
        return setChannel;
    }

    public Stack<AbstractCommand> getCommandStack() {
        return commandStack;
    }
    public MessageBuilder getBuilder() {
        return builder;
    }
    public HashMap<String, ICommand> getCommands() {
        return commands;
    }
    public ICommand getCommand (String command){
        command = command.replace("!","");

        if (!commands.containsKey(command)){
            return null;
        }

        return commands.get(command);
    }

}
