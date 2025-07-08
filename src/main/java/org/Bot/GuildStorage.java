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
    Type type = new TypeToken<HashMap<String, String>>(){}.getType();
    ArrayList<GuildSetting> listOfGuildSettings;
    HashMap<String,String> mapToStore;
    HashSet<Snowflake> keyset;
    File jsonFile;

    void initialize(){
        jsonParser = new GsonBuilder().setPrettyPrinting().create();
        jsonFile = new File(filePath);
        listOfGuildSettings = new ArrayList<>();
        keyset = new HashSet<>();
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
            storedJson = loadJson();
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
            mapToStore = new HashMap<>();
            listOfGuildSettings.forEach(e-> {
                mapToStore.put(e.guildID,e.channelId);
            });
            jsonParser.toJson(mapToStore,writer);
            return true;
        } catch (IOException e) {
            log.debug("Cannot find such file");
            return false;
        }

    }
    HashMap<Snowflake,Snowflake> loadJson(){
        try(BufferedReader reader = new BufferedReader(new FileReader(jsonFile) )) {
            HashMap<String, String> parsed =jsonParser.fromJson(reader, type);
            return convertToSnowFlake(parsed);
        } catch (FileNotFoundException e) {
            log.warn("Cannot find file");
        } catch (IOException e) {
            log.warn("Cannot find file");
        }
        return null;
    }


    /*
    Converts the json file
     */
    private HashMap<Snowflake, Snowflake> convertToSnowFlake(HashMap<String, String> parsed) {
        HashMap<Snowflake,Snowflake> savedChannels = new HashMap<>();
        for (String key : parsed.keySet()) {
            Snowflake guildID = Snowflake.of(key);
            Snowflake channelID = Snowflake.of(parsed.get(key));
            keyset.add(guildID);
            savedChannels.put(guildID,channelID);
        }
        return savedChannels;
    }

    public HashMap<Snowflake,Snowflake> getChannels() {
        return this.storedJson;
    }


    /**
     * Adds it to the channel and return a hashmap of the recently added channel
     * Creates a new instance of the guild setting based on the guild id and channel id
     * @param message
     */
    public HashMap<Snowflake,Snowflake> addChannel(Message message) {
        Optional<Snowflake> guildOptional  = message.getGuildId();
        if (message.getGuildId().isEmpty()){
            return null;
        }
        Snowflake guildID = guildOptional.get();
        storedJson.put(guildID,message.getChannelId());
        keyset.add(guildID);
        listOfGuildSettings.add(
                new GuildSetting((guildID).asString(),(message.getChannelId()).asString()));
        HashMap<Snowflake, Snowflake> map = new HashMap<>();
         map.put(guildID,message.getChannelId());
         store();
         return map;
    }

    public HashSet<Snowflake> getKeyset() {
        return keyset;
    }
}
