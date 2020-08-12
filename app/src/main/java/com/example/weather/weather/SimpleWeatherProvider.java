package com.example.weather.weather;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedHashMap;

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


}
