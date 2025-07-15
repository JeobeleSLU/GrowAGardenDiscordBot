/*
Author: Turtle :)
 */
package org.Bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import org.BaseClasses.GuildReference;
import org.BaseClasses.GuildSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.storage.Store;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

public class GuildStorage implements Store {
    private static final Logger log = LoggerFactory.getLogger(GuildStorage.class);
    Gson jsonParser;
    String filePath = "src/main/resources/guilds.json";
    private static final Type GUILD_SETTINGS_TYPE = new TypeToken<ArrayList<GuildSetting>>(){}.getType();
    ArrayList<GuildSetting> listOfGuildSettings;
    File jsonFile;
    HashSet<GuildReference> guildObject;
    GuildReference recentlyAdded;
    void initialize(){
        jsonParser = new GsonBuilder().setPrettyPrinting().create();
        jsonFile = new File(filePath);
        listOfGuildSettings = new ArrayList<>();
    }
    public boolean addData(GuildSetting setting){
        listOfGuildSettings.removeIf(e -> e.getGuildID().equals(setting.getGuildID()));
        listOfGuildSettings.add(setting);
        return true;
    }
    public boolean removeSubscription(Message message){

        return true;
    }
    /**
     * Will store the guild id and channel id inside the json file
     */
    public GuildStorage() {
        initialize();
        if (!isJsonEmpty() ){
            loadJson();
        }else {
            guildObject = new HashSet<>();
            listOfGuildSettings = new ArrayList<>();
        }
    }
    private void createJson() {
        try {
            jsonFile.createNewFile();
        } catch (IOException e) {
            log.warn("Cannot create json file!");
        }
    }

    /**
     * Returns true if json file is empty
     * @return
     */
    private boolean isJsonEmpty() {
        if (!jsonFile.exists()){
            createJson();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile))) {
            return reader.readLine() == null;
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }
    /**
     * Save the Guilds inside json in a string format
     * @return
     */
    @Override
    public boolean store() {
        try(BufferedWriter writer= new BufferedWriter(new FileWriter(jsonFile))) {
            jsonParser.toJson(listOfGuildSettings,writer);
            return true;
        } catch (IOException e) {
            log.debug("Cannot find such file");
            return false;
        }
    }
    void loadJson(){
        try(BufferedReader reader = new BufferedReader(new FileReader(jsonFile) )) {
            guildObject = new HashSet<>();
            ArrayList<GuildSetting> settings = jsonParser.fromJson(reader, GUILD_SETTINGS_TYPE);
            storeReferences(settings);
        } catch (FileNotFoundException e) {
            log.warn("Cannot find file");
        } catch (IOException e) {
            log.warn("Cannot find file");
        }
    }
    /*
    Converts the json file
     */
    private void storeReferences(ArrayList<GuildSetting> parsed) {
        for (GuildSetting settings : parsed) {
            Snowflake guildId = convertToSnowFlake(settings.getGuildID());
            Snowflake channelID = convertToSnowFlake(settings.getChannelID());
            if (guildId == null || channelID == null){
                log.warn("Channel is null");
                continue;
            }
            guildObject.add(new GuildReference(guildId,channelID,settings.getRoles()));
        }
    }
    private Snowflake convertToSnowFlake(String guildID) {
        if (guildID.isEmpty()){
            log.warn("Guild is null: " + guildID);
            return null;
        }
        return Snowflake.of(guildID);
    }

    public GuildReference addRole(Message message) {
        if (message.getGuildId().isEmpty()){
            return null;
        }
        if (!guildExists(message)){
            return null;
        }
        GuildSetting setting = getGuild(message);
        String role = extractRole(message.getContent());
        if (setting == null || role ==null){
            return null;
        }
        setting.addRole(role);
        GuildReference reference = getGuildReference(message);
        if (reference == null){
            return null;
        }
        reference.addRole(role);
        store();
        return reference;
    }

    private GuildReference getGuildReference(Message message) {
        return guildObject.stream()
                .filter(e -> e.getGuildID().equals(message.getGuildId().get()))
                .findFirst()
                .orElse(null); //
    }

    /**
     * Extracts the role based on space, and returns the role name
     * for example !setrole admin
     * returns the admin
     * @param content
     * @return
     */
    private String extractRole(String content) {
        String[] splitted = content.split(" ");
        if (splitted.length != 2){
            return null;
        }
        return splitted[1];
    }
    private GuildSetting getGuild(Message message) {
        for (GuildSetting settings : listOfGuildSettings){
            if (settings.getGuildID().equals(message.getGuildId().get().asString())){
                return settings;
            }
        }
        return null;
    }
    public ArrayList<GuildSetting> getListOfGuildSettings() {
        return listOfGuildSettings;
    }

    public HashSet<GuildReference> getGuildObject() {
        return guildObject;
    }

    public GuildReference getRecentGuild() {
        return this.recentlyAdded;
    }

    public synchronized boolean addChannel(Message message) {
        Optional<Snowflake> optionalGuildID = message.getGuildId();
        if (optionalGuildID.isEmpty()){
            return false;
        }
        if(!guildExists(message)){
            addToStorage(message);
          return true;
        }
        updateChannel(message);
        return true;
    }

    private void updateChannel(Message message) {
        guildObject.stream().findAny().get().setChannel(message.getChannelId());
    }
    private void addToStorage(Message message) {
        Optional<Snowflake> optionalGuildID = message.getGuildId();
        Snowflake guildIDReference = optionalGuildID.get();
        Snowflake channelID = message.getChannelId();
        GuildSetting temp = new GuildSetting(guildIDReference.asString(),channelID.asString());
        addData(temp);
        this.recentlyAdded = new GuildReference(optionalGuildID.get(),channelID);
        store();
    }

    /**
     * Returns true if the guild exists inside the guildObject
     * @param message
     * @return
     */
    private boolean guildExists(Message message) {
        //Search here
        Optional<Snowflake> optional = message.getGuildId();
        GuildReference temporaryReference = new GuildReference(optional.get(),message.getChannelId());
        return guildObject.stream().anyMatch(e-> e.equals(temporaryReference));
    }
}
