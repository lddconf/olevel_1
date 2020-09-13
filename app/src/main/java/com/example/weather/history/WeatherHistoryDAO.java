package com.example.weather.history;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WeatherHistoryDAO {

    @Insert
    void insertWeatherHistory(WeatherHistory weatherHistory);

    @Delete
    void deleteWeatherHistory(WeatherHistory weatherHistory);

    @Query("SELECT COUNT(*) FROM WeatherHistory")
    int getRecordsCount();

    @Query("SELECT * from WeatherHistory")
    List<WeatherHistory> getAllRecords();
}
