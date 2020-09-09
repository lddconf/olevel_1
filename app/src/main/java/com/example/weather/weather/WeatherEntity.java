package com.example.weather.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.weather.R;
import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.WeatherData;

import java.io.Serializable;

import static com.example.weather.weatherprovider.openweatherorg.OpenWeatherOrgProvider.convertWeatherImageID2BuildIn;

public class WeatherEntity implements Serializable {
    private int temperature;
    private int feelsLike;
    private double windSpeed;
    private int pressureBar;

    @Nullable
    private String cloudiness;

    private boolean isFahrenheit;

    @Nullable
    private String externalIconID;

    public WeatherEntity() {
        this((int)(Math.random() * 40), (int)(Math.random() * 40), (Math.random() * 10), 765 - (int)(Math.random()*10), "cloudy", false, null);
    }

    public WeatherEntity(int temperature, int feelsLike, double windSpeed,
                         int pressureBar, String cloudiness, boolean isFahrenheit,
                         @Nullable String externalIconID) {
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.windSpeed = windSpeed;
        this.pressureBar = pressureBar;
        this.cloudiness = cloudiness;
        this.isFahrenheit = isFahrenheit;
        this.externalIconID = externalIconID;
    }

    public WeatherEntity(@NonNull WeatherEntity entity) {
        this.temperature = entity.temperature;
        this.feelsLike = entity.feelsLike;
        this.windSpeed = entity.windSpeed;
        this.pressureBar = entity.pressureBar;
        this.cloudiness = entity.cloudiness;
        this.isFahrenheit = entity.isFahrenheit;
        this.externalIconID = entity.externalIconID;
    }

    public WeatherEntity(final WeatherData wd) {
        this( (int)Math.round(wd.getMain().getTemp()),
                (int)Math.round(wd.getMain().getFeelsLike()),
                wd.getWind().getSpeed(), wd.getMain().getPressure(),
                wd.getWeather()[0].getMain(),
                false, wd.getWeather()[0].getIcon() );
    }

    public WeatherEntity toFahrenheitUnits() {
        if ( isFahrenheit ) {
            return new WeatherEntity(this);
        } else {
            float inFahrenheitTemp = Math.round(getTemperature()*1.8f + 32f);
            float inFahrenheitFeelsLike = Math.round(getFeelsLikeTemp()*1.8f + 32f);
            return new WeatherEntity((int)inFahrenheitTemp, (int)inFahrenheitFeelsLike, getWindSpeed(), getPressureBar(), getCloudiness(), true, null);
        }
    }

    public WeatherEntity toCelsiusUnits() {
        if ( !isFahrenheit ) {
            return new WeatherEntity(this);
        } else {
            float inCelsiusTemp = Math.round((getTemperature() - 32f) / 1.8f);
            float inCelsiusFeelsLike = Math.round((getTemperature() - 32f) / 1.8f);
            return new WeatherEntity((int)inCelsiusTemp, (int)inCelsiusFeelsLike, getWindSpeed(), getPressureBar(), getCloudiness(), false, null);
        }
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

    public int getBuildInIconID() {
        if ( externalIconID == null ) {
            return R.mipmap.ic_weather_na;
        }
        return convertWeatherImageID2BuildIn( externalIconID );
    }

    public String getExternalIconID() {
        return externalIconID;
    }
}
