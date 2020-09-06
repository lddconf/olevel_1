package com.example.weather.weatherprovider;

import androidx.annotation.Nullable;

import com.example.weather.CityID;
import com.example.weather.weather.WeatherEntity;

import java.util.ArrayList;

public interface WeatherProviderInterface {

    @Nullable
    WeatherEntity getWeatherFor(CityID city);

    @Nullable
    ArrayList<WeatherEntity> getWeatherWeekForecastFor(CityID city);
}
