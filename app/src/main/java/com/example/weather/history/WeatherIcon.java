package com.example.weather.history;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.weather.weather.WeatherEntity;

@Entity
public class WeatherIcon {
    @PrimaryKey(autoGenerate = true)
    public int iconId;

    @ColumnInfo(name = "icon")
    public String iconTextID;

    public static WeatherIcon makeFrom(WeatherEntity weatherEntity) {
        WeatherIcon weatherIcon = new WeatherIcon();
        weatherIcon.iconTextID = weatherEntity == null ? null: weatherEntity.getExternalIconID();
        return weatherIcon;
    }
}
