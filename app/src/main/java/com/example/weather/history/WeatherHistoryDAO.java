package com.example.weather.history;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface WeatherHistoryDAO {

    @Insert
    void insertWeather(WeatherHistory weatherHistory);

    @Insert
    void insertIcon(WeatherIcons icons);

    @Insert
    void insertCity(City city);



}
