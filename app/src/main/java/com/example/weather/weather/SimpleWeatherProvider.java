package com.example.weather.weather;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Hashtable;

public class SimpleWeatherProvider implements WeatherProviderInterface {
    private Hashtable<String, WeatherEntity> weathers;

    public SimpleWeatherProvider() {
        weathers = new Hashtable<>();
        String[] cities = new String[] {"Moscow", "New York", "Berlin", "Paris", "Prague", "Minsk"};
        Arrays.sort(cities);

        for ( String key : cities ) {
            weathers.put(key, new WeatherEntity());
        }
    }

    @Override
    public String[] getCitiesList() {
        String[] cities = new String[weathers.keySet().size()];
        weathers.keySet().toArray(cities);
        return cities;
    }

    @Override
    @Nullable
    public WeatherEntity getWeatherFor(String city) {
        return weathers.get(city);
    }


}
