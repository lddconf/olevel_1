package com.example.weather.weatherprovider.openweatherorg.currentWeatherModel;

import com.google.gson.annotations.SerializedName;

public class PrecipitationForecast {
    @SerializedName("1h")
    private float oneHour;

    @SerializedName("3h")
    private float threeHours;

    public float getOneHour() {
        return oneHour;
    }

    public float getThreeHours() {
        return threeHours;
    }

    public void setOneHour(float oneHour) {
        this.oneHour = oneHour;
    }

    public void setThreeHours(float threeHours) {
        this.threeHours = threeHours;
    }
}
