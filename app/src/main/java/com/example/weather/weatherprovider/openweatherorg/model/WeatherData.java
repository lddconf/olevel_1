package com.example.weather.weatherprovider.openweatherorg.model;

import com.example.weather.weather.WeatherEntity;

public class WeatherData {
    private Coord coord;
    private Weather[] weather;
    private Main main;
    private Wind wind;
    private Clouds clouds;
    private Sys sys;
    private String name;

    //Request code
    private int cod;

    //Message for error handling
    private String message;

    public Coord getCoord() {
        return coord;
    }

    public Weather[] getWeather() {
        return weather;
    }

    public Main getMain() {
        return main;
    }

    public Wind getWind() {
        return wind;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public Sys getSys() {
        return sys;
    }

    public String getName() {
        return name;
    }

    public WeatherEntity toWeatherEntity() {
        return new WeatherEntity( (int)Math.round(getMain().getTemp()), (int)Math.round(getMain().getFeels_like()), getWind().getSpeed(), getMain().getPressure(), getWeather()[0].getMain(), false );
    }

    public int getCode() {
        return cod;
    }

    public String getErrorMessage() {
        return message;
    }
}


