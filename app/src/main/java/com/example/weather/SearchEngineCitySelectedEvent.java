package com.example.weather;

public class SearchEngineCitySelectedEvent {
    private UserSettings.CityID cityID;

    public SearchEngineCitySelectedEvent(UserSettings.CityID cityID) {
        this.cityID = cityID;
    }

    public UserSettings.CityID getCityID() {
        return cityID;
    }
}
