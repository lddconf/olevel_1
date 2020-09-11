package com.example.weather.history;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class City {
    @PrimaryKey(autoGenerate = true)
    public int cityId;

    @ColumnInfo(name = "cityUniqueID")
    public int cityUniqueID;

    @ColumnInfo(name = "name")
    public String name;
}
