package com.example.weather;

import com.example.weather.weather.CityWeatherSettings;

import java.io.Serializable;

/**
 * Simple class for main activity settings
 */
public class MainActivitySettings implements Serializable {
    private CityWeatherSettings weather;

    /**
     * Default constructor for serializable/deserializable objects
     */
    public MainActivitySettings() {
        weather = new CityWeatherSettings();
    }

    /**
     * Setup new city
     * @param city name of specified location
     */
    public void setCity(String city) {
        weather.setCurrentCity(city);
    }

    /**
     * Get current city
     * @return current city
     */
    public String getCity() {
        return weather.getCurrentCity();
    }

    /**
     * Get weather settings
     * @return weather settings
     */
    public CityWeatherSettings getWeatherSettings() {
        return weather;
    }
}