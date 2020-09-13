package com.example.weather.history;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.weather.CityID;
import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.Weather;

@Entity( indices = {@Index("cityUniqueID")})
public class WeatherCity {
    @PrimaryKey(autoGenerate = true)
    public int cityId;

    @ColumnInfo(name = "cityUniqueID")
    public int cityUniqueID;

    @ColumnInfo( name = "Country")
    public String country;

    @ColumnInfo(name = "name")
    public String name;

    public static WeatherCity makeFrom(@NonNull CityID cityID) {
        WeatherCity weatherCity = new WeatherCity();
        weatherCity.cityUniqueID = cityID.getId();
        weatherCity.country = cityID.getCountry();
        weatherCity.name = cityID.getName();
        return weatherCity;
    }
}
