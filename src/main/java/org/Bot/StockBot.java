package org.Bot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import org.Console.ConsoleMessage;

import java.util.ArrayList;

public class StockBot implements Runnable,NotificationHandler,WeatherAlert, ConsoleMessage {
    GatewayDiscordClient gateway;
    DiscordClient client;
    ChannelNotifier notifier = new ChannelNotifier();
    String botToken ;
    Obeserver observer;
    ArrayList<Item> lastStock;
    GuildStorage storedGuilds;


    public StockBot(String botToken,Obeserver obeserver) {
        this.botToken = botToken;
        this.observer = obeserver;
    }
    private void connect(String botToken) {
        client = DiscordClient.create(botToken);
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
        notifier.initComponents(storedGuilds);
        notifier.setDiscordGateway(gateway);
    }

    private void listenToCommands() {
        startBot();
        gateway.on(MessageCreateEvent.class)
                .subscribe(event -> {
                    Message message = event.getMessage();
                    String content = message.getContent();

                    if("!sendStocks".equalsIgnoreCase(content)){
                        notifier.notifyChannel(message,lastStock,gateway);
                    }else if("!setChannel".equalsIgnoreCase(content)){
                        setChannel(message);
                    } else if ("!Hello".equalsIgnoreCase(content)) {
                        sendWorld(message);
                    } else if ("!ping".equalsIgnoreCase(content)) {
                        sendPong(message);
                    } else if ("!setRole".equalsIgnoreCase(content)) {
                        setRole(message);
                    }
                });
    }

    private void setRole(Message message) {
        GuildReference reference = storedGuilds.addRole(message);

        if (reference == null){
            notifier.errorAdding(message);
        }
        notifier.updateRole(reference);

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
    notifier.notifyChannel(message,lastStock,gateway);
    }

    @Override
    public void triggerEventNotification(String message) {
        notifier.notifyMessage(message,gateway);
    }
    public void sendToDevConsole(Exception ex) {
        notifier.sendToDevConsoles(ex,gateway);
    }
    @Override
    public void nottifyWeather(ArrayList<String> weather) {
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
