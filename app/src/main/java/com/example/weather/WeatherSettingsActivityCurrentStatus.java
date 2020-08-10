package com.example.weather;

import androidx.annotation.NonNull;

import com.example.weather.diplayoption.WeatherDisplayOptions;

import java.io.Serializable;

public class WeatherSettingsActivityCurrentStatus implements Serializable {
    private String city;
    private WeatherDisplayOptions displayOptions;

    public WeatherSettingsActivityCurrentStatus() {
        this("Paris", new WeatherDisplayOptions());
    }

    public WeatherSettingsActivityCurrentStatus(String city, WeatherDisplayOptions displayOptions) {
        this.city = city;
        this.displayOptions = displayOptions;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Get display options entity
     * @return DisplayOptions variable
     */
    public WeatherDisplayOptions getDisplayOptions() {
        return displayOptions;
    }

    public void setDisplayOptions(@NonNull WeatherDisplayOptions options) {
        displayOptions = options;
    }

}