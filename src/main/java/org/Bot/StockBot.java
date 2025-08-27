package org.Bot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import org.BaseClasses.GuildReference;
import org.BaseClasses.Item;
import org.BaseClasses.Weather;
import org.Commands.CommandHandler;
import org.Commands.ICommand;
import org.Console.ConsoleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.storage.GuildStorage;
import org.storage.AddChannel;

import java.util.ArrayList;
import java.util.Stack;

public class StockBot implements Runnable,NotificationHandler,WeatherAlert, ConsoleMessage {
    private static final Logger log = LoggerFactory.getLogger(StockBot.class);
    GatewayDiscordClient gateway;
    ChannelNotifier notifier = new ChannelNotifier();
    StockNotifier notification = notifier.getStockNotifier();
    String botToken ;
    Obeserver observer;
    ArrayList<Item> lastStock;
    static GuildStorage storedGuilds;
    AddChannel guildSaver;
    CommandHandler commandHandler;

    public StockBot(String botToken,Obeserver obeserver) {
        this.botToken = botToken;
        this.observer = obeserver;
    }
    private void connect(String botToken) {
        gateway = DiscordClient.create(botToken)
                .gateway()
                .setEnabledIntents(IntentSet.nonPrivileged().or(IntentSet.of(Intent.MESSAGE_CONTENT)))
                .login()
                .block();

    }
    public void sendStock(ArrayList<Item> items){
        lastStock = items;
        notifier.notifyStock(items,gateway);
    }
    @Override
    public void run() {
        if (botToken.isEmpty()) {
            System.err.println("Bot token missing.");
            return;
        }
        connect(botToken);
        if (observer != null) {
            observer.initCon(this);
        }
        System.out.println("Hello?");
        initComponents();
        listenToCommands();
        gateway.onDisconnect().block();
    }

    private void initComponents() {
        storedGuilds = new GuildStorage();
        this.guildSaver = storedGuilds;
        notifier.initComponents(storedGuilds);
        notifier.setDiscordGateway(gateway);
        this.commandHandler = new CommandHandler();
        commandHandler.initComponents();
    }

    private void listenToCommands() {
        startBot();
        gateway.on(MessageCreateEvent.class)
                .subscribe(event -> {
                    Message message = event.getMessage();
                    //Early return so that it would not get processed
                    String content = message.getContent();
                    if (content.isEmpty()) return;
                    if('!' != content.charAt(0)) return;
                   ICommand command =  commandHandler.getCommand(content);
                   if (command == null){
                       sendUnknownCommand(message);
                   }
                    command.execute(message,gateway);
                });
    }
    private void setRole(Message message) {
        GuildReference reference = storedGuilds.addRole(message);
        if (reference == null){
            notifier.errorAdding(message);
            log.warn("Cannot add role");
            System.out.println("cannot add role");
        }
        notifier.updateRole(reference);
        System.out.println("Role added");
    }
    private void sendUnknownCommand(Message message) {
        message.getChannel().flatMap(channel -> channel.createMessage(message.getContent()+
                " is an unknown command")).subscribe();
    }

    private void sendPong(Message message) {
    message.getChannel()
            .flatMap(channel -> channel.createMessage("Pong!"))
            .subscribe();
}

private void sendWorld(Message message) {
        message.getChannel()
                .flatMap(channel -> channel.createMessage("World!"))
                .subscribe();
    }


    private void startBot() {
        gateway.on(ReadyEvent.class)
                .doOnNext(event -> {
                    notifier.notifyBotOnline(gateway);
                })
                .subscribe();
    }

    /**
     * Sets the channel and add it to the guild storage
     * add it to the map of the channel notifier
     * @param message
     */
    private void setChannel(Message message) {
        if (!storedGuilds.addChannel(message)){
            return;
        }
    notifier.notifyChannel(message);
    }
    @Override
    public void triggerEventNotification(String message) {
        notifier.notifyMessage(message,gateway);
    }
    public void sendToDevConsole(Exception ex) {
        notifier.sendToDevConsoles(ex,gateway);
    }
    @Override
    public void nottifyWeather(Stack<Weather> weather) {
        notifier.alertWeather(weather,gateway);
    }

    @Override
    public void sendMessage(String message) {
        notifier.sendMessage(message);
    }
    @Override
    public void mentionRoleAndSend(String message) {
        notifier.mentionEveryone(message);
    }
}
