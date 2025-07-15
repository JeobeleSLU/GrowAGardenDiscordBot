package org.Bot;


import net.bytebuddy.implementation.bytecode.Addition;

import java.util.ArrayList;
import java.util.Objects;

public class GuildSetting {
    String channelId;
    String guildID;
    ArrayList<String> roles;
    public GuildSetting(String guildID,String channelId) {
        this.channelId = channelId;
        this.guildID = guildID;
        roles = new ArrayList<>();
    }
    void addRole(String roleToAdd){
        roles.add(roleToAdd);
    }

    public ArrayList<String> getRoles() {
        return roles;
    }

    public String getGuildID() {
        return guildID;
    }

    public String getChannelID() {
        return this.channelId;
    }

}
