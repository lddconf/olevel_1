package com.example.weather.weatherprovider.openweatherorg.currentWeatherModel;

public class Weather {
    private String main;
    private String description;
    private String icon;

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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
