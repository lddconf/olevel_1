package com.example.weather.weatherprovider.openweatherorg.currentWeatherModel;

import com.google.gson.annotations.SerializedName;

public class PrecipitationForecast {
    @SerializedName("1h")
    private float one_hour;
    @SerializedName("3h")
    private float three_hours;

    public float getOne_hour() {
        return one_hour;
    }

    public float getThree_hours() {
        return three_hours;
    }

    public void setOne_hour(float one_hour) {
        this.one_hour = one_hour;
    }

    public void setThree_hours(float three_hours) {
        this.three_hours = three_hours;
    }
}
