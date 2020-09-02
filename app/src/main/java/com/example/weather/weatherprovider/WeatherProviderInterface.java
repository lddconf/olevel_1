package com.example.weather.weatherprovider;

import androidx.annotation.Nullable;

import com.example.weather.UserSettings;
import com.example.weather.weather.WeatherEntity;

import java.util.ArrayList;

public interface WeatherProviderInterface {

    @Nullable
    WeatherEntity getWeatherFor(UserSettings.CityID city);

    @Nullable
    ArrayList<WeatherEntity> getWeatherWeekForecastFor(UserSettings.CityID city);
}
