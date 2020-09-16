package com.example.weather;

import android.app.Application;

import androidx.room.Room;

import com.example.weather.history.WeatherCityDAO;
import com.example.weather.history.WeatherHistoryDAO;
import com.example.weather.history.WeatherIconsDAO;

public class WeatherApp extends Application {
    private static WeatherApp instance;

    private WeatherDataBase database;

    public static WeatherApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        database = Room.databaseBuilder(
          getApplicationContext(),
          WeatherDataBase.class,
          "weather_database")
                .build();
    }

    public WeatherCityDAO getWeatherCityDAO() {
        return database.getWeatherCityDAO();
    }

    public WeatherIconsDAO getWeatherIconsDAO() {
        return database.getWeatherIconsDAO();
    }

    public WeatherHistoryDAO getWeatherHistoryDAO() {
        return database.getWeatherHistoryDAO();
    }

}
