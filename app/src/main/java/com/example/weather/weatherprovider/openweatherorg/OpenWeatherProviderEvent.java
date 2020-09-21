package com.example.weather.weatherprovider.openweatherorg;

import com.example.weather.CityID;

import java.io.Serializable;

public class OpenWeatherProviderEvent implements Serializable {
    private OWeatherResult resultCode;
    private CityID city;
    private String errorDescription;
    private float longitude;
    private float latitude;

    public enum OWeatherResult implements Serializable {
        REQUEST_COMPLETED_KEYWORD, REQUEST_COMPLETED_LAT_LONG, CONNECTION_ERROR;
    }

    public OpenWeatherProviderEvent( OWeatherResult code, CityID city ) {
        this(code, city, null);
    }

    public OpenWeatherProviderEvent(OWeatherResult resultCode, CityID city, String errorDescription) {
        this.resultCode = resultCode;
        this.city = city;
        this.errorDescription = errorDescription;
    }

    public OWeatherResult getResultCode() {
        return resultCode;
    }

    public CityID getCity() {
        return city;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}
