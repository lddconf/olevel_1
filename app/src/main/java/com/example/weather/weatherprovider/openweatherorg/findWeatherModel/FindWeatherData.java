package com.example.weather.weatherprovider.openweatherorg.findWeatherModel;

import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.Clouds;
import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.Coord;
import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.Main;
import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.PrecipitationForecast;
import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.Sys;
import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.Weather;
import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.Wind;

public class FindWeatherData {
    private int id;
    private String name;
    private Coord coord;
    private Main main;
    private int dt;
    private Wind wind;
    private Sys sys;
    private PrecipitationForecast rain;
    private PrecipitationForecast show;
    private Clouds clouds;
    private Weather[] weather;

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public void setDt(int dt) {
        this.dt = dt;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }

    public void setRain(PrecipitationForecast rain) {
        this.rain = rain;
    }

    public void setShow(PrecipitationForecast show) {
        this.show = show;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    public void setWeather(Weather[] weather) {
        this.weather = weather;
    }

    public PrecipitationForecast getRain() {
        return rain;
    }

    public PrecipitationForecast getShow() {
        return show;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coord getCoord() {
        return coord;
    }

    public Main getMain() {
        return main;
    }

    public int getDt() {
        return dt;
    }

    public Wind getWind() {
        return wind;
    }

    public Sys getSys() {
        return sys;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public Weather[] getWeather() {
        return weather;
    }
}
