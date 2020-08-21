package com.example.weather.diplayoption;

import com.example.weather.R;

import java.io.Serializable;

/**
 * Display options class
 */
public class WeatherDisplayOptions implements Serializable {
    private boolean useFahrenheitTempUnit;
    private boolean showWindSpeed;
    private boolean showPressure;
    private boolean showFeelsLike;
    private int themeId;

    public WeatherDisplayOptions() {
        this(true, true, true, true, R.style.AppThemeLight);
    }

    public WeatherDisplayOptions(boolean useFahrenheitTempUnit, boolean showWindSpeed, boolean showPressure, boolean showFeelsLike, int themeId) {
        this.useFahrenheitTempUnit = useFahrenheitTempUnit;
        this.showWindSpeed = showWindSpeed;
        this.showPressure = showPressure;
        this.showFeelsLike = showFeelsLike;
        this.themeId = themeId;
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

    public int getThemeId() {
        return themeId;
    }

    public void setThemeLight() {
        this.themeId = R.style.AppThemeLight;
    }

    public void setThemeDark() {
        this.themeId = R.style.AppThemeDark;
    }

    public boolean isLightTheme() {
        return (themeId == R.style.AppThemeLight);
    }
}
