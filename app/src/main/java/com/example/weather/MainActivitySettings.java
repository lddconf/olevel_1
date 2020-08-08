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
    private boolean useCelsiusUnit;
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
        useCelsiusUnit = true;
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
    public int getFeelsLikeTemp() {
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
    public boolean useCelsiusUnit() {
        return useCelsiusUnit;
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

    /**
     * Setup new city
     * @param city name of specified location
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Setup temperature
     * @param temperature temperature value
     */
    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    /**
     * Setup feels like temperature
     * @param feelsLike temperature value
     */
    public void setFeelsLikeTemperature(int feelsLike) {
        this.feelsLike = feelsLike;
    }

    /**
     * Setup temperature unit
     * @param isCelsiusUnit if true using celsius notation
     */
    public void setUseCelsiusUnit(boolean isCelsiusUnit) {
        if ( this.useCelsiusUnit == isCelsiusUnit ) return;

        if ( isCelsiusUnit && (!this.useCelsiusUnit()) ) {
            //Recalculate for celsius to fahrenheit
            float inCelsius = Math.round((getTemperature() - 32f) / 1.8f);
            setTemperature((int)inCelsius);

            inCelsius = Math.round((getFeelsLikeTemp() - 32f) / 1.8f);
            setFeelsLikeTemperature((int)inCelsius);
        } else  {
            //Recalculate from fahrenheit to celsius
            float inFahrenheit = Math.round(getTemperature()*1.8f + 32f);
            setTemperature((int)inFahrenheit);

            inFahrenheit = Math.round(getFeelsLikeTemp()*1.8f + 32f);
            setFeelsLikeTemperature((int)inFahrenheit);
        }
        this.useCelsiusUnit = isCelsiusUnit;
    }


    /**
     * Setup feels like field
     * @param showFeelsLike display feels like field
     */
    public void setShowFeelsLike(boolean showFeelsLike) {
        this.showFeelsLike = showFeelsLike;
    }

    /**
     * Setup pressure field
     * @param showPressure display pressure field
     */
    public void setShowPressure(boolean showPressure) {
        this.showPressure = showPressure;
    }

    /**
     * Setup wind field
     * @param showWind display wind field
     */
    public void setShowWind(boolean showWind) {
        this.showWind = showWind;
    }
}