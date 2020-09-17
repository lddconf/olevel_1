package com.example.weather.history;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.weather.weather.WeatherEntity;
import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.Weather;

import java.util.Calendar;

public class WeatherHistoryWithCityAndIcon {
    @Embedded
    public WeatherHistory history;

    @Embedded
    public WeatherCity city;

    @Embedded
    public WeatherIcon icon;

    public static WeatherEntity make(WeatherHistoryWithCityAndIcon weatherHistoryWithCityAndIcon) {
        return new WeatherEntity(
                (int)weatherHistoryWithCityAndIcon.history.temp,
                (int)weatherHistoryWithCityAndIcon.history.feelsLike,
                weatherHistoryWithCityAndIcon.history.windSpeed,
                weatherHistoryWithCityAndIcon.history.pressureBar,
                null,
                weatherHistoryWithCityAndIcon.history.isFahrenheit,
                weatherHistoryWithCityAndIcon.icon.iconTextID
        );
    }
}
