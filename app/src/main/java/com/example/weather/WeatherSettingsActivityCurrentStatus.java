package com.example.weather;

import java.io.Serializable;

class WeatherSettingsActivityCurrentStatus implements Serializable {
    private String city;
    private boolean useFahrenheitTempUnit;
    private boolean showWindSpeed;
    private boolean showPressure;
    private boolean showFeelsLike;

    public WeatherSettingsActivityCurrentStatus() {
        this("Paris", false, false, false, false);
    }

    public WeatherSettingsActivityCurrentStatus(String city, boolean useFahrenheitTempUnit, boolean showWindSpeed, boolean showPressure, boolean showFeelsLike) {
        this.city = city;
        this.useFahrenheitTempUnit = useFahrenheitTempUnit;
        this.showWindSpeed = showWindSpeed;
        this.showPressure = showPressure;
        this.showFeelsLike = showFeelsLike;
    }

    public WeatherSettingsActivityCurrentStatus(MainActivitySettings settings) {
        this.city = settings.getCity();
        this.useFahrenheitTempUnit = !settings.useCelsiusUnit();
        this.showWindSpeed = settings.isShowWind();
        this.showPressure = settings.isShowPressure();
        this.showFeelsLike = settings.isShowFeelsLike();
    }

    public String getCity() {
        return city;
    }

    public boolean isFahrenheitTempUnit() {
        return useFahrenheitTempUnit;
    }

    public boolean isShowWindSpeed() {
        return showWindSpeed;
    }

    public boolean isShowPressure() {
        return showPressure;
    }

    public boolean isShowFeelsLike() {
        return showFeelsLike;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setTemperatureUnit(boolean useFahrenheitTempUnit) {
        this.useFahrenheitTempUnit = useFahrenheitTempUnit;
    }

    public void setShowWindSpeed(boolean showWindSpeed) {
        this.showWindSpeed = showWindSpeed;
    }

    public void setShowPressure(boolean showPressure) {
        this.showPressure = showPressure;
    }

    public void setShowFeelsLike(boolean showFeelsLike) {
        this.showFeelsLike = showFeelsLike;
    }
}