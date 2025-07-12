/*
Author: Turtle :)
 */
package org.Bot;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.storage.Store;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public class GuildStorage implements Store {
    private static final Logger log = LoggerFactory.getLogger(GuildStorage.class);
    HashMap<Snowflake, Snowflake> storedJson;
    Gson jsonParser;
    String filePath = "src/main/resources/guilds.json";
    private static final Type GUILD_SETTINGS_TYPE = new TypeToken<ArrayList<GuildSetting>>(){}.getType();

    ArrayList<GuildSetting> listOfGuildSettings;
    HashSet<Snowflake> keyset;
    File jsonFile;
    ArrayList<GuildReference> guildObject;
    void initialize(){
        jsonParser = new GsonBuilder().setPrettyPrinting().create();
        jsonFile = new File(filePath);
        listOfGuildSettings = new ArrayList<>();
        keyset = new HashSet<>();
        guildObject = new ArrayList<>();
    }

    public boolean addData(GuildSetting setting){
        listOfGuildSettings.removeIf(e -> e.guildID.equals(setting.guildID));
        listOfGuildSettings.add(setting);
        return true;
    }
    public boolean removeSubscription(GuildSetting settings){
        listOfGuildSettings.removeIf(e-> e.guildID.equalsIgnoreCase(settings.guildID));
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
            storedJson = new HashMap<>();
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

    public HashMap<Snowflake,Snowflake> getChannels() {
        return this.storedJson;
    }


    /**
     * Adds it to the channel and return a hashmap of the recently added channel
     * Creates a new instance of the guild setting based on the guild id and channel id
     * @param message
     */
    public HashMap<Snowflake, Snowflake> addChannel(Message message) {
        Optional<Snowflake> guildOptional  = message.getGuildId();
        if (message.getGuildId().isEmpty()){
            System.out.println("Guild id is null");
            return null;
        }
        Snowflake guildID = guildOptional.get();
        storedJson.put(guildID,message.getChannelId());
        keyset.add(guildID);
        listOfGuildSettings.add(
                new GuildSetting((guildID).asString(),(message.getChannelId()).asString()));
        HashMap<Snowflake, Snowflake> map = new HashMap<>();
         map.put(guildID,message.getChannelId());
         guildObject.add(new GuildReference(guildID,message.getChannelId()));
         syncPersistent();
         return map;
    }

    private void syncPersistent() {
        store();
    }

    public HashSet<Snowflake> getKeyset() {
        return keyset;
    }

    public void addRole(Message message) {
        GuildSetting setting = getGuild(message);
        String role = extractRole(message.getContent());
        if (setting == null || role ==null){
            return;
        }
        setting.addRole(role);
        store();
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
}
