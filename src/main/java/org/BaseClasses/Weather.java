package org.BaseClasses;

public class Weather {
    long timeStarted;
    long timeEnded;
    long timeDuration;
    String weatherName;
    String weatherID;
    String description;

    public Weather(long timeStarted, long timeEnded, long timeDuration, String weatherName, String weatherID) {
        this.timeStarted = timeStarted;
        this.timeEnded = timeEnded;
        this.timeDuration = timeDuration;
        this.weatherName = weatherName;
        this.weatherID = weatherID;
    }

    public long getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(long timeStarted) {
        this.timeStarted = timeStarted;
    }

    public long getTimeEnded() {
        return timeEnded;
    }

    public void setTimeEnded(long timeEnded) {
        this.timeEnded = timeEnded;
    }

    public long getTimeDuration() {
        return timeDuration;
    }

    public void setTimeDuration(long timeDuration) {
        this.timeDuration = timeDuration;
    }

    public String getWeatherName() {
        return weatherName;
    }

    public void setWeatherName(String weatherName) {
        this.weatherName = weatherName;
    }

    public String getWeatherID() {
        return weatherID;
    }

    public void setWeatherID(String weatherID) {
        this.weatherID = weatherID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
