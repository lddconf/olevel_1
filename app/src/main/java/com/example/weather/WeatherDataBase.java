package com.example.weather;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.weather.history.WeatherCity;
import com.example.weather.history.WeatherCityDAO;
import com.example.weather.history.WeatherHistory;
import com.example.weather.history.WeatherHistoryDAO;
import com.example.weather.history.WeatherIcon;
import com.example.weather.history.WeatherIconsDAO;

@Database(entities = {WeatherCity.class, WeatherHistory.class, WeatherIcon.class}, version = 1, exportSchema = false)
public abstract class WeatherDataBase extends RoomDatabase {
    public abstract WeatherCityDAO getWeatherCityDAO();
    public abstract WeatherIconsDAO getWeatherIconsDAO();
    public abstract WeatherHistoryDAO getWeatherHistoryDAO();
}
