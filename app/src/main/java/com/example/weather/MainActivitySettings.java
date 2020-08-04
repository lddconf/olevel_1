package com.example.weather;

import java.io.Serializable;

/**
 * Simple class for main activity settings
 */
class MainActivitySettings implements Serializable {
    private int temperature;
    private int feelsLike;
    private double windSpeed;
    private int pressureBar;
    private String cloudiness;
    private String city;
    private String tempUnit;
    private boolean showFeelsLike;
    private boolean showPressure;
    private boolean showWind;

    /**
     * Default constructor for serializable/deserializable objects
     */
    public MainActivitySettings() {
        temperature = (int)(Math.random() * 40);
        feelsLike = (int)(Math.random() * 40);
        city = "Moscow";
        cloudiness = "cloudy";
        tempUnit = "\u2103";
        windSpeed = (Math.random() * 10);
        pressureBar = 765 - (int)(Math.random()*10);
        showFeelsLike = true;
        showPressure = true;
        showWind = true;
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

    /**
     * Get wind speed in m/s
     * @return wind speed
     */
    public double getWindSpeed() {
        return windSpeed;
    }

    /**
     * Get pressure in mm of mercury
     * @return pressure
     */
    public int getPressureBar() {
        return pressureBar;
    }

    /**
     * Display Feels Like view
     * @return true for display feels like field
     */
    public boolean isShowFeelsLike() {
        return showFeelsLike;
    }

    /**
     * Display pressure bar
     * @return true for display pressure field
     */
    public boolean isShowPressure() {
        return showPressure;
    }

    /**
     * Display wind speed
     * @return true for display wind speed field
     */
    public boolean isShowWind() {
        return showWind;
    }
}