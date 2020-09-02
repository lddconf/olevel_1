package com.example.weather;

public class SearchEngineCitySelectedEvent {
    private CityID cityID;

    public SearchEngineCitySelectedEvent(CityID cityID) {
        this.cityID = cityID;
    }

    public CityID getCityID() {
        return cityID;
    }
}
