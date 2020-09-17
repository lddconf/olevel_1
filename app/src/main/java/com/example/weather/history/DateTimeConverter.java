package com.example.weather.history;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateTimeConverter {

    @TypeConverter
    public long fromDateTime(Date date) {
        return date == null ? 0 :date.getTime();
    }

    @TypeConverter
    public Date toDateTime(long date) {
        return new Date(date);
    }
}
