package com.example.weather.history;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

@Dao
public abstract class WeatherIconsDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(WeatherIcon weatherIcon);

    public long getOrMakeIconId(WeatherIcon icon) {
        long id = getWeatherIconIDByIconTextID(icon.iconTextID);
        if ( id == 0L ) {
            id = insert(icon);
        }
        return id;
    }

    @Update
    public abstract void update(WeatherIcon weatherIcon);

    @Delete
    public abstract void delete(WeatherIcon weatherIcon);

    @Query("DELETE FROM WeatherIcon WHERE iconId = :id")
    public abstract void deleteById(long id);

    @Query("SELECT * FROM WeatherIcon WHERE iconId = :id")
    public abstract WeatherIcon getWeatherIconById(long id);

    @Query("SELECT * FROM WeatherIcon WHERE icon = :id")
    public abstract WeatherIcon getWeatherIconByIconTextID(String id);

    @Query("SELECT iconId FROM WeatherIcon WHERE icon = :id")
    public abstract long getWeatherIconIDByIconTextID(String id);
}
