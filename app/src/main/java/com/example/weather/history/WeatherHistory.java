package com.example.weather.history;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class WeatherHistory {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "temp")
    public float temp;

    @ColumnInfo(name = "feels_like")
    public float feelsLike;

    @ColumnInfo(name = "wind_speed")
    public double windSpeed;

    @ColumnInfo( name = "pressure_kpa")
    public int pressureBar;

    @ColumnInfo( name = "iconId")
    private int iconId;

    @ColumnInfo( name = "City" )
    public int cityId;
}
