package com.example.weather.history;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.Weather;

@Dao
public abstract class WeatherCityDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(WeatherCity weatherCity);

    public long getOrMakeCityId(WeatherCity weatherCity) {
        long id = getIdByCityUniqueID(weatherCity.cityUniqueID);
        if ( id == 0L ) {
            id = insert(weatherCity);
        }
        return id;
    }

    @Update
    public abstract void update(WeatherCity weatherCity);

    @Delete
    public abstract void delete(WeatherCity weatherCity);

    @Query("DELETE FROM WeatherCity WHERE cityId = :id")
    public abstract void deleteById(long id);

    @Query("SELECT * FROM WeatherCity WHERE cityId = :id")
    public abstract WeatherCity getWeatherCityById(long id);

    @Query("SELECT cityId FROM WeatherCity WHERE cityUniqueID = :id")
    public abstract long getIdByCityUniqueID(int id);
}
