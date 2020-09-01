package com.example.weather.weatherprovider.openweatherorg;

public class OpenWeatherProviderEvent {
    OWeatherResult resultCode;
    String         city;
    String         errorDescription;

    public enum OWeatherResult {
        UPDATE_SUCCESSFUL, CONNECTION_ERROR, CITY_NOT_FOUND_ERROR;
    }

    public OpenWeatherProviderEvent( OWeatherResult code, String city ) {
        this(code, city, null);
    }

    public OpenWeatherProviderEvent(OWeatherResult resultCode, String city, String errorDescription) {
        this.resultCode = resultCode;
        this.city = city;
        this.errorDescription = errorDescription;
    }

    public OWeatherResult getResultCode() {
        return resultCode;
    }

    public String getCity() {
        return city;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}
