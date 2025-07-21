package org.Bot;

import org.BaseClasses.Weather;

import java.util.Stack;

public interface WeatherAlert {
    void nottifyWeather(Stack<Weather> weathers);
}
