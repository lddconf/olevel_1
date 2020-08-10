package com.example.weather.weather;

import androidx.annotation.NonNull;

import com.example.weather.diplayoption.WeatherDisplayOptions;

import java.io.Serializable;

public class CityWeatherSettings implements Serializable {
    private WeatherEntity currentWeather;
    private String currentCity;

    private WeatherDisplayOptions displayOptions;

    /**
     * Default constructor for serializable/deserializable objects
     */
    public CityWeatherSettings() {
        this("Moscow", new WeatherEntity(), new WeatherDisplayOptions() );
    }


    public CityWeatherSettings(String city, WeatherEntity weather, WeatherDisplayOptions options) {
        this.currentCity = city;
        this.currentWeather = weather;
        setWeatherDisplayOptions(options);
    }

    /**
     * Setup new city
     * @param city name of specified location
     */
    public void setCurrentCity(String city) {
        this.currentCity = city;
    }

    /**
     * Get current city
     * @return current city
     */
    public String getCurrentCity() {
        return currentCity;
    }


    /**
     *  Get weather display options
     * @return display options
     */
    @NonNull
    public WeatherDisplayOptions getWeatherDisplayOptions() { return displayOptions; }

    /**
     * Set weather display options
     * @param options new options
     */
    public void setWeatherDisplayOptions(@NonNull WeatherDisplayOptions options) {
        displayOptions = options;
        setWeather(currentWeather);
    }

    /**
     * Get current weather
     * @return current weather
     */
    @NonNull
    public WeatherEntity getWeather() {
        return currentWeather;
    }

    /**
     * Setup new weather
     * @param entity - new weather entity
     */
    public void setWeather(@NonNull WeatherEntity entity ) {
        if ( displayOptions.isFahrenheitTempUnit() == entity.isFahrenheitTempUnit() ) {
            currentWeather = entity;
        } else {
            //In fahrenheit. Need in celsius
            if ( entity.isFahrenheitTempUnit() && (!displayOptions.isFahrenheitTempUnit())) {
                currentWeather = entity.toCelsiusUnits();
            } else {
                //From celsius to fahrenheit
                currentWeather = entity.toFahrenheitUnits();
            }
        }
    }
}
