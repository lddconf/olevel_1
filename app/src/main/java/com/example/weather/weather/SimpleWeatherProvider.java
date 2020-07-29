package com.example.weather.weather;

import java.util.Arrays;

public class SimpleWeatherProvider implements WeatherProviderInterface {
    @Override
    public String[] getCitiesList() {
        String[] cities = new String[] {"Moscow", "New York", "Berlin", "Paris"};
        Arrays.sort(cities);
        return cities;
    }
}
