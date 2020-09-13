package com.example.weather.history;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.weather.weather.WeatherEntity;

import java.util.Calendar;
import java.util.Date;

@Entity(foreignKeys = {
        @ForeignKey(entity = WeatherIcon.class,
        parentColumns = "iconId",
        childColumns = "iconId",
        onDelete = ForeignKey.SET_NULL),
        @ForeignKey(entity = WeatherCity.class,
        parentColumns = "cityId",
        childColumns = "cityId",
        onDelete = ForeignKey.CASCADE)
},
        indices = {@Index(value = {"cityId"}),
                @Index(value = {"iconId"})})
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

    @ColumnInfo( name = "update_time")
    @TypeConverters({DateTimeConverter.class})
    public Date timestamp;

    @ColumnInfo( name = "iconId")
    public long iconId;

    @ColumnInfo( name = "cityId" )
    public long cityId;

    public static WeatherHistory make(WeatherEntity weatherEntity, long cityId, long iconId) {
        WeatherHistory weatherHistory = new WeatherHistory();
        weatherHistory.cityId = cityId;
        weatherHistory.iconId = iconId;
        weatherHistory.feelsLike = weatherEntity.getFeelsLikeTemp();
        weatherHistory.pressureBar = weatherEntity.getPressureBar();
        weatherHistory.temp = weatherEntity.getTemperature();
        weatherHistory.windSpeed = weatherEntity.getWindSpeed();
        weatherHistory.timestamp = Calendar.getInstance().getTime();
        return weatherHistory;
    }

}
