package com.example.weather.weather;

import androidx.annotation.NonNull;

import com.example.weather.diplayoption.WeatherDisplayOptions;

import java.io.Serializable;
import java.util.ArrayList;

public class CityWeatherSettings implements Serializable {
    private WeatherEntity currentWeather;
    private String currentCity;

    private ArrayList<WeatherEntity> weekForecast;

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
        weekForecast = new ArrayList<>(14);
        for (int i = 0; i < 14; i++) {
            weekForecast.add( new WeatherEntity());
        }

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
        applyWeekForecastOptions();
    }

    /**
     * Add hourly forecast options
     * @param hourlyForecast new forecast options
     */
    public void addWeekForecastWeather(ArrayList<WeatherEntity>  hourlyForecast) {
        this.weekForecast = hourlyForecast;
        applyWeekForecastOptions();
    }

    /**
     * Get hourly forecast weather
     * @return hourly forecast weather
     */
    ArrayList<WeatherEntity> getWeekForecast() {
        return weekForecast;
    }
    /**
     * Apply weather settings
     */
    private void applyWeekForecastOptions() {
        for (int i = 0; i < weekForecast.size(); i++) {
            weekForecast.set(i, formatWeatherWithOptions(weekForecast.get(i), displayOptions));
        }
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
        currentWeather = formatWeatherWithOptions(entity, displayOptions);
    }

    private WeatherEntity formatWeatherWithOptions(@NonNull WeatherEntity entity, @NonNull WeatherDisplayOptions options) {
        if ( options.isFahrenheitTempUnit() == entity.isFahrenheitTempUnit() ) {
            return entity;
        } else {
            WeatherEntity newWeather;
            //In fahrenheit. Need in celsius
            if ( entity.isFahrenheitTempUnit() && (!options.isFahrenheitTempUnit())) {
                newWeather = entity.toCelsiusUnits();
            } else {
                //From celsius to fahrenheit
                newWeather = entity.toFahrenheitUnits();
            }
            return newWeather;
        }
    }
}
