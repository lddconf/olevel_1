package com.example.weather;

import java.io.Serializable;

/**
 * Simple class for main activity settings
 */
class MainActivitySettings implements Serializable {
    private int temperature;
    private int feelsLike;
    private String cloudiness;
    private String city;
    private String tempUnit;

    /**
     * Default constructor for serializable/deserializable objects
     */
    public MainActivitySettings() {
        temperature = (int)(Math.random() * 40);
        feelsLike = (int)(Math.random() * 40);
        city = "Moscow";
        cloudiness = "cloudy";
        tempUnit = "\u2103";
    }

    /**
     * Get current temperature
     * @return current temperature
     */
    public int getTemperature() {
        return temperature;
    }

    /**
     * Get current feels like temperature
     * @return feels like temperature
     */
    public int getFeelsLike() {
        return feelsLike;
    }

    /**
     * Get current cloudiness status
     * @return cloudiness status string
     */
    public String getCloudiness() {
        return cloudiness;
    }

    /**
     * Get current city
     * @return current city
     */
    public String getCity() {
        return city;
    }

    /**
     * Get current temperature display unit
     * @return current temperature unit
     */
    public String getTempUnit() {
        return tempUnit;
    }
}