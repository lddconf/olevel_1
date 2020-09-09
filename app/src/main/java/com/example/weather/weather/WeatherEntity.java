package com.example.weather.weather;

import com.example.weather.R;
import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.Weather;
import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.WeatherData;

import java.io.Serializable;

import static com.example.weather.weatherprovider.openweatherorg.OpenWeatherOrgProvider.convertWeatherImageID2Custom;

public class WeatherEntity implements Serializable {
    private int temperature;
    private int feelsLike;
    private double windSpeed;
    private int pressureBar;
    private String cloudiness;
    private boolean isFahrenheit;
    private int iconID;

    public WeatherEntity() {
        this((int)(Math.random() * 40), (int)(Math.random() * 40), (Math.random() * 10), 765 - (int)(Math.random()*10), "cloudy", false, R.mipmap.ic_weather_na);
    }

    public WeatherEntity(int temperature, int feelsLike, double windSpeed, int pressureBar, String cloudiness, boolean isFahrenheit, int icon_id) {
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.windSpeed = windSpeed;
        this.pressureBar = pressureBar;
        this.cloudiness = cloudiness;
        this.isFahrenheit = isFahrenheit;
        this.iconID = icon_id;
    }

    public WeatherEntity(WeatherEntity entity) {
        this.temperature = entity.temperature;
        this.feelsLike = entity.feelsLike;
        this.windSpeed = entity.windSpeed;
        this.pressureBar = entity.pressureBar;
        this.cloudiness = entity.cloudiness;
        this.isFahrenheit = entity.isFahrenheit;
    }

    public WeatherEntity toFahrenheitUnits() {
        if ( isFahrenheit ) {
            return new WeatherEntity(this);
        } else {
            float inFahrenheitTemp = Math.round(getTemperature()*1.8f + 32f);
            float inFahrenheitFeelsLike = Math.round(getFeelsLikeTemp()*1.8f + 32f);
            return new WeatherEntity((int)inFahrenheitTemp, (int)inFahrenheitFeelsLike, getWindSpeed(), getPressureBar(), getCloudiness(), true, getIconID());
        }
    }

    public WeatherEntity toCelsiusUnits() {
        if ( !isFahrenheit ) {
            return new WeatherEntity(this);
        } else {
            float inCelsiusTemp = Math.round((getTemperature() - 32f) / 1.8f);
            float inCelsiusFeelsLike = Math.round((getTemperature() - 32f) / 1.8f);
            return new WeatherEntity((int)inCelsiusTemp, (int)inCelsiusFeelsLike, getWindSpeed(), getPressureBar(), getCloudiness(), false, getIconID());
        }
    }

    public WeatherEntity(final WeatherData wd) {
        this( (int)Math.round(wd.getMain().getTemp()),
                (int)Math.round(wd.getMain().getFeels_like()),
                wd.getWind().getSpeed(), wd.getMain().getPressure(),
                wd.getWeather()[0].getMain(),
                false, convertWeatherImageID2Custom(wd.getWeather()[0].getIcon()) );
    }

    /**
     * Get current temperature
     * @return current temperature
     */
    public int getTemperature() {
        return temperature;
    }

    /**
     * Get current feels like temperature
     * @return feels like temperature
     */
    public int getFeelsLikeTemp() {
        return feelsLike;
    }

    /**
     * Get wind speed in m/s
     * @return wind speed
     */
    public double getWindSpeed() {
        return windSpeed;
    }

    /**
     * Get pressure in mm of mercury
     * @return pressure
     */
    public int getPressureBar() {
        return pressureBar;
    }

    /**
     * Get current cloudiness status
     * @return cloudiness status string
    */
    public String getCloudiness() {
        return cloudiness;
    }


    public boolean isFahrenheitTempUnit() {
        return isFahrenheit;
    }

    public int getIconID() {
        return iconID;
    }
}
