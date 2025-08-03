package org.Bot;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.BaseClasses.GuildReference;
import org.BaseClasses.Item;
import org.BaseClasses.Weather;
import org.Utiilities.IStock;
import org.Utiilities.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class ChannelNotifier implements IStock {
    boolean isTravellingMerchantPresent = false;
    static int cycleCounter = 1;
    GatewayDiscordClient gateway;
    HashSet<GuildReference> guilds;
    GuildStorage storedGuilds;
    boolean isMasterInStock= false;
    MessageBuilder messageService;

    public ChannelNotifier() {
         guilds =new HashSet<>();
    }
    void initComponents(GuildStorage storage){
        this.storedGuilds = storage;
        this.messageService = new MessageBuilder(this);
        if (!storedGuilds.getGuildObject().isEmpty()){
            guilds.addAll(storedGuilds.getGuildObject());
        }

    };

    public void notifyStock(ArrayList<Item> items, GatewayDiscordClient client) {
        EmbedCreateSpec.Builder stock = messageService.getStockMessage(items);
        if (isMasterInStock){
            mentionEveryone("Master in stock!");
        }
       sendEmbed(stock,client);
    }
    public void notifyChannel(Message message, ArrayList<Item> lastStock, GatewayDiscordClient client) {
        GuildReference recentChannel = new GuildReference(message.getGuildId().get(),message.getChannelId());
        guilds.add(recentChannel);
        if (lastStock == null || lastStock.isEmpty()) {
            client.getChannelById(message.getChannelId())
                    .ofType(MessageChannel.class)
                    .flatMap(channel -> channel.createMessage("Can't get previous stock."))
                    .subscribe();
            return;
        }
        EmbedCreateSpec stock = messageService.getStockMessage(lastStock).build();
        client.getChannelById(message.getChannelId())
                .ofType(MessageChannel.class)
                .flatMapMany(channel ->channel.createMessage(stock)
                )
                .subscribe();
        }

    public void notifyBotOnline(GatewayDiscordClient client) {
        Flux.fromIterable(guilds)
                .flatMap(guildId -> {
                    Snowflake channelId = guildId.getChannelID();
                    return client.getChannelById(channelId)
                            .ofType(MessageChannel.class)
                            .flatMap(channel -> channel.createMessage("```I am fully online sending stocks..... 🌱```\n\n"));
                })
                .subscribe();
    }

    public void notifyMessage(String message, GatewayDiscordClient client) {
        EmbedCreateSpec.Builder embedBuilder = messageService.notifyMessage(message);

        if (isTravellingMerchantPresent){
            mentionEveryone(message);
        }
        System.out.println("Cycle: " + cycleCounter);
        cycleCounter++;
        sendEmbed(embedBuilder,client);
    }
    Stack<Snowflake> getGuilds(){
        Stack<Snowflake> channels = new Stack<>();
        guilds.stream()
                .map(GuildReference::getChannelID)
                .filter(Objects::nonNull)
                .forEach(channels::add);
        return channels;
    }

    private void sendEmbed(EmbedCreateSpec.Builder embedBuilder, GatewayDiscordClient client) {
        Stack<Snowflake> channels = getGuilds();
        while (!channels.isEmpty()) {
            Snowflake channelId = channels.pop();
        client.getChannelById(channelId)
                .ofType(MessageChannel.class)
                .flatMap(channel -> channel.createEmbed(embedBuilder.build())).retryWhen(Retry.backoff(5, Duration.ofSeconds(3)))
                .doOnError(error -> System.err.println("Failed to send embed: " + error.getMessage()))
                .subscribe();
    }
    }
    public void sendToDevConsoles(Exception ex, GatewayDiscordClient gateway) {

    }

    public void alertWeather(Stack<Weather> weatherStack, GatewayDiscordClient client) {
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .title("☁️ Weather : ")
                .color(Color.GRAY_CHATEAU)
                .timestamp(Instant.now());
        if (weatherStack.size() == 1){
            Weather weather = weatherStack.pop();
            embedBuilder.addField("Current Weather: ",weather.getWeatherName(),true);
            String start = formatDiscordTimestamp(weather.getTimeStarted());
            String end = formatDiscordTimestamp(weather.getTimeEnded());
            embedBuilder.addField("Start Time:  ",start,false);
            embedBuilder.addField("End time :  ",end,false);
        }else {
            String extractedString = extractWeathers(weatherStack);
            embedBuilder.addField("Current Weather: ", extractedString, true);
        }
        sendEmbed(embedBuilder,this.gateway);

    }
    private String formatDiscordTimestamp(long epochMillis) {
        return "<t:" + epochMillis + ":F>"; // ":F" = full date and time
    }

    private String extractWeathers(Stack<Weather> weathers) {
        StringBuilder builder = new StringBuilder();
        builder.append("[ ");
        while (!weathers.empty()){
            Weather weather = weathers.pop();
            if (weather == null){
                continue;
            }
            if (weathers.size() != 1){
                builder.append(weather.getWeatherName()).append(" + ");
            }else{
                builder.append(weather.getWeatherName());
            }
        }
        builder.append("]");
        return builder.toString();
    }

    //Mention everyone for something
    void mentionEveryone(String Reason){
        Flux.fromIterable(guilds)
                .flatMap(guildID -> {
                    if (guildID.getRoles().isEmpty()) {
                        return Mono.empty();
                    }
                    String mention = "@"+guildID.getRoles().get(0);
                    Snowflake channelId = guildID.getChannelID();
                    return gateway.getChannelById(channelId)
                            .ofType(MessageChannel.class)
                            .flatMap(channel -> channel.createMessage(mention + " "+ Reason));
                })
                .subscribe();

    }
    public void setDiscordGateway(GatewayDiscordClient gateway) {
        this.gateway = gateway;
    }

    public void errorAdding(Message message) {
         gateway.getChannelById(message.getChannelId())
                .ofType(MessageChannel.class)
                .flatMap(channel ->
                        channel.createMessage("Please set a channel first before adding a role "))
                 .subscribe();
    }

    public void updateRole(GuildReference reference) {
        guilds.remove(reference);
        guilds.add(reference);
    }
    public void sendMessage(String message) {
        Flux.fromIterable(guilds)
                .flatMap(guildID -> {
                    Snowflake channelId = guildID.getChannelID();
                    return gateway.getChannelById(channelId)
                            .ofType(MessageChannel.class)
                            .flatMap(channel -> channel.createMessage(message));
                })
                .subscribe();
    }

    @Override
    public void setMasterInStock(boolean isMasterInStock) {
        this.isMasterInStock = isMasterInStock;
    }

    @Override
    public void setTravellingMerchant(boolean isTravellingMerchantPresent) {
        this.isTravellingMerchantPresent = isTravellingMerchantPresent;

    }

    @Override
    public void reset() {
        this.isTravellingMerchantPresent = false ;
        this.isMasterInStock = false ;
    }

}
