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

    public long getHistoryRecordsCount() {
        synchronized (weatherHistoryDAO) {
            if ( history != null ) {
                return history.size();
            }
            return 0;
        }
    }

    public void reloadHistoryFromDB(Long id) {
        synchronized (weatherHistoryDAO) {
            if (id == null) {
                if (cityId != null || history == null) {
                    history = weatherHistoryDAO.getAllRecordsWithCityAndIcon();
                    Collections.reverse(history); //Reverse order
                    cityId = id;
                }
            } else {
                if (cityId == null || history == null || !cityId.equals(id)) {
                    history = weatherHistoryDAO.getAllRecordsWithCityAndIcon(id);
                    Collections.reverse(history); //Reverse order
                    cityId = id;
                }
            }
        }
    }

    public List<WeatherHistoryWithCityAndIcon> getHistory() {
        if ( history != null ) {
            reloadHistoryFromDB(null);
            synchronized (weatherHistoryDAO) {
                return history;
            }
        }
        return null;
    }

    public void removeHistory(@NonNull WeatherHistory hist) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                weatherHistoryDAO.deleteWeatherHistory(hist);
                synchronized (weatherHistoryDAO) {
                    history = null; //For reload purpose
                }
            }
        }).start();

    }

    public void addHistory(@NonNull WeatherHistory hist) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                weatherHistoryDAO.insertWeatherHistory(hist);
                synchronized (weatherHistoryDAO) {
                    history = null; //For reload purpose
                }
            }
        }).start();
    }
}
