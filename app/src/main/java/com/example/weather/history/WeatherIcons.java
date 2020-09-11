package com.example.weather.history;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class WeatherIcons {
    @PrimaryKey(autoGenerate = true)
    public int iconId;

    @ColumnInfo(name = "icon")
    public String iconTextID;
}
