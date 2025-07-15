package org.BaseClasses;

import discord4j.common.util.Snowflake;

import java.util.ArrayList;
import java.util.Objects;

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
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GuildReference)) return false;
        GuildReference that = (GuildReference) obj;
        return this.guildID.equals(that.guildID);
    }
    @Override
    public int hashCode() {
        return Objects.hash(guildID);
    }
    public void setChannel(Snowflake channelId) {
        this.channelID = channelId;
    }

    public void addRole(String role) {
        if (roleExists(role)){
            return;
        }
        roles.add(role);
    }

    private boolean roleExists(String role) {
        return (roles.stream().anyMatch(e -> e.equals(role)));
    }
}
