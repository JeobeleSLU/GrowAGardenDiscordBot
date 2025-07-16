package org.BaseClasses;


import java.util.ArrayList;

public class GuildSetting {
    String channelId;
    String guildID;
    ArrayList<String> roles;
    public GuildSetting(String guildID,String channelId) {
        this.channelId = channelId;
        this.guildID = guildID;
        roles = new ArrayList<>();
    }
    public GuildSetting(String guildID,String channelId,ArrayList<String> roles) {
        this.channelId = channelId;
        this.guildID = guildID;
        this.roles = roles;
    }
    public void addRole(String role) {
        if (roleExists(role)){
            return;
        }
        roles.add(role);
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
    private boolean roleExists(String role) {
        return (roles.stream().anyMatch(e -> e.equals(role)));
    }
    public void setChannelID(String channelID) {
        this.channelId = channelID;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GuildReference)) return false;
        GuildReference that = (GuildReference) obj;
        return this.guildID.equals(that.guildID);
    }
}
