package com.example.weather.weather;

import androidx.annotation.Nullable;

public interface WeatherProviderInterface {
    /**
     * Get supported cities list
     * @return cities array
     */
    String[] getCitiesList();

    @Nullable
    WeatherEntity getWeatherFor(String city);
}
