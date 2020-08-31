package com.example.weather.weatherprovider.openweatherorg.model;

public class Weather {
    private String main;
    private String description;

    public void setMain(String main) {
        this.main = main;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMain() {
        return main;
    }

    public String getDescription() {
        return description;
    }
}
