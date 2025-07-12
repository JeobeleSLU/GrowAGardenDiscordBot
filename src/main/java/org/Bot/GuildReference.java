package org.Bot;

import discord4j.common.util.Snowflake;

import java.util.ArrayList;

public class GuildReference {
    Snowflake guildID;
    Snowflake channelID;
    ArrayList<String> roles;

    public GuildReference(Snowflake guildID, Snowflake channelID) {
        this.guildID = guildID;
        this.channelID = channelID;
        roles = new ArrayList<>();
    }

    public GuildReference(Snowflake guildId, Snowflake channelID, ArrayList<String> roles) {
        this.guildID = guildId;
        this.channelID = channelID;
        this.roles = roles;
    }

    public Snowflake getChannelID() {
        return channelID;
    }

    public ArrayList<String> getRoles() {
        return roles;
    }

    public Snowflake getGuildID() {
        return guildID;
    }
}
