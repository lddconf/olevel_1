package com.example.weather.weather;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Simple weather provider. Generate random weather data for determined number of cities
 */
public class SimpleWeatherProvider implements WeatherProviderInterface {
    private LinkedHashMap<String, WeatherEntity> weathers;

    public SimpleWeatherProvider() {
        weathers = new LinkedHashMap<>();
        String[] cities = new String[] {"Moscow", "New York", "Berlin", "Paris", "Prague", "Minsk"};
        Arrays.sort(cities);

        for ( String key : cities ) {
            weathers.put(key, new WeatherEntity());
        }
    }

    @Override
    public String[] getCitiesList() {
        String[] cities = new String[weathers.keySet().size()];
        return weathers.keySet().toArray(cities);
    }

    @Override
    @Nullable
    public WeatherEntity getWeatherFor(String city) {
        return weathers.get(city);
    }

    @Nullable
    @Override
    public ArrayList<WeatherEntity> getWeatherWeekForecastFor(String city) {
        WeatherEntity w = weathers.get(city);
        if ( w != null ) {
            ArrayList<WeatherEntity> result = new ArrayList<>(14);
            for (int i = 0; i < 14; i++) {
                result.add(new WeatherEntity());
            }
            return result;
        }
        return null;
    }
}
