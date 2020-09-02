package com.example.weather.weatherprovider.openweatherorg.currentWeatherModel;

public class Wind {
    private float speed;
    private int deg;

    public float getSpeed() {
        return speed;
    }

    public int getDeg() {
        return deg;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setDeg(int deg) {
        this.deg = deg;
    }
}
