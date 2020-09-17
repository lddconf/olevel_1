package com.example.weather.history.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.weather.CityID;
import com.example.weather.history.WeatherCityDAO;
import com.example.weather.history.WeatherHistory;
import com.example.weather.history.WeatherHistoryDAO;
import com.example.weather.history.WeatherHistoryWithCityAndIcon;
import com.example.weather.history.WeatherIconsDAO;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class HistorySource {
    private final WeatherCityDAO weatherCityDAO;
    private final WeatherIconsDAO weatherIconsDAO;
    private final WeatherHistoryDAO weatherHistoryDAO;

    List<WeatherHistoryWithCityAndIcon> history;
    Long cityId;

    public HistorySource(@NonNull WeatherCityDAO weatherCityDAO,
                         @NonNull WeatherIconsDAO weatherIconsDAO,
                         @NonNull WeatherHistoryDAO weatherHistoryDAO) {
        this.weatherCityDAO = weatherCityDAO;
        this.weatherIconsDAO = weatherIconsDAO;
        this.weatherHistoryDAO = weatherHistoryDAO;
    }

    public long getHistoryRecordsCount(@Nullable CityID city) {
        if ( city == null ) {
            return weatherHistoryDAO.getRecordsCount();
        } else {
            long cityId = weatherCityDAO.getIdByCityUniqueID(city.getId());
            if ( cityId > 0 ) {
                return weatherHistoryDAO.getRecordsCount(cityId);
            }
            return 0;
        }
    }

    private void loadHistory(Long id) {
        if ( id == null ) {
            if ( cityId != null || history == null ) {
                history = weatherHistoryDAO.getAllRecordsWithCityAndIcon();
                Collections.reverse(history); //Reverse order
                cityId = id;
            }
        } else {
            if ( cityId == null || history == null || !cityId.equals(id)) {
                history = weatherHistoryDAO.getAllRecordsWithCityAndIcon(id);
                Collections.reverse(history); //Reverse order
                cityId = id;
            }
        }
    }

    public List<WeatherHistoryWithCityAndIcon> getHistory(@Nullable CityID city) {
        if ( city == null ) {
            loadHistory(null);
            return history;
        } else {
            long cityId = weatherCityDAO.getIdByCityUniqueID(city.getId());
            if ( cityId > 0 ) {
                loadHistory(Long.valueOf(cityId));
                return history;
            }
            return null;
        }
    }

    public void removeHistory(@NonNull WeatherHistory history) {
        weatherHistoryDAO.deleteWeatherHistory(history);
        history = null; //For reload purpose
    }

    public void addHistory(@NonNull WeatherHistory history) {
        weatherHistoryDAO.insertWeatherHistory(history);
        history = null; //For reload purpose
    }
}
