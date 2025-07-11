package org.Bot;

import discord4j.core.GatewayDiscordClient;

import java.util.ArrayList;

public interface WeatherAlert {
    void nottifyWeather(ArrayList<String> weathers);
}
