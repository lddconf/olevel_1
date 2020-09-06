package com.example.weather.weatherprovider.openweatherorg.currentWeatherModel;

import java.io.Serializable;

public class Coord implements Serializable {
    private float lon;
    private float lat;

    public float getLon() {
        return lon;
    }

    public float getLat() {
        return lat;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }
}
