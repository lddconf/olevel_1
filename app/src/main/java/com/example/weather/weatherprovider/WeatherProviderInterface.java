package com.example.weather.weatherprovider;

import androidx.annotation.Nullable;

import com.example.weather.weather.WeatherEntity;

import java.util.ArrayList;

public interface WeatherProviderInterface {
    
    @Nullable
    WeatherEntity getWeatherFor(String city);

    @Nullable
    ArrayList<WeatherEntity> getWeatherWeekForecastFor(String city);
}
