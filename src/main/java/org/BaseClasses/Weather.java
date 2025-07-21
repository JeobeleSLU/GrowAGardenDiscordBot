package org.BaseClasses;

public class Weather {
    long timeActivated;
    String weatherName;
    String weatherID;
    String description;

    public Weather(long timeActivated, String weatherName, String weatherID) {
        this.timeActivated = timeActivated;
        this.weatherName = weatherName;
        this.weatherID = weatherID;
    }

    public long getTimeActivated() {
        return timeActivated;
    }

    public void setTimeActivated(long timeActivated) {
        this.timeActivated = timeActivated;
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
