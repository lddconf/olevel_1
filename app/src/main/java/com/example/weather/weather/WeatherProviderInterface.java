package com.example.weather.weather;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public interface WeatherProviderInterface {
    /**
     * Get supported cities list
     * @return cities array
     */
    String[] getCitiesList();

    @Nullable
    WeatherEntity getWeatherFor(String city);

    @Nullable
    ArrayList<WeatherEntity> getWeatherWeekForecastFor(String city);
}
