package com.example.weather.history;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface WeatherHistoryDAO {

    @Insert
    void insertWeatherHistory(WeatherHistory weatherHistory);

    @Delete
    void deleteWeatherHistory(WeatherHistory weatherHistory);

    @Query("SELECT COUNT(*) FROM WeatherHistory")
    int getRecordsCount();

    @Query("SELECT COUNT(*) FROM WeatherHistory WHERE _cityId = :id")
    long getRecordsCount(long id);

    @Query("SELECT * from WeatherHistory")
    List<WeatherHistory> getAllRecords();

    @Query("SELECT * from WeatherHistory WHERE _cityId = :id")
    List<WeatherHistory> getAllRecords(long id);

    @Transaction
    @Query("SELECT * FROM WeatherHistory " +
            "join WeatherCity ON WeatherHistory._cityId = WeatherCity.cityId " +
            "join WeatherIcon ON WeatherHistory._iconId = WeatherIcon.iconId")
    List<WeatherHistoryWithCityAndIcon> getAllRecordsWithCityAndIcon();

    @Transaction
    @Query("select * from (select * from WeatherHistory WHERE _cityId = :id) as WH " +
            "join WeatherCity ON WH._cityId = WeatherCity.cityId " +
            "join WeatherIcon ON WH._iconId = WeatherIcon.iconId")
    List<WeatherHistoryWithCityAndIcon> getAllRecordsWithCityAndIcon(long id);
}
