package com.example.weather.weatherprovider.openweatherorg;

import com.example.weather.UserSettings;

public class OpenWeatherProviderEvent {
    OWeatherResult resultCode;
    UserSettings.CityID city;
    String         errorDescription;

    public enum OWeatherResult {
        REQUEST_COMPLETED, CONNECTION_ERROR;
    }

    public OpenWeatherProviderEvent( OWeatherResult code, UserSettings.CityID city ) {
        this(code, city, null);
    }

    public OpenWeatherProviderEvent(OWeatherResult resultCode, UserSettings.CityID city, String errorDescription) {
        this.resultCode = resultCode;
        this.city = city;
        this.errorDescription = errorDescription;
    }

    public OWeatherResult getResultCode() {
        return resultCode;
    }

    public UserSettings.CityID getCity() {
        return city;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}
