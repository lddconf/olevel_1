package com.example.weather.weatherprovider.openweatherorg;

import androidx.annotation.Nullable;

import com.example.weather.CityID;
import com.example.weather.R;
import com.example.weather.weather.WeatherEntity;
import com.example.weather.weatherprovider.OpenWeatherOrgRequest;
import com.example.weather.weatherprovider.WeatherProviderInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;

import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.WeatherData;
import com.example.weather.weatherprovider.openweatherorg.findWeatherModel.FindData;
import com.example.weather.weatherprovider.openweatherorg.findWeatherModel.FindWeatherData;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class OpenWeatherOrgProvider implements WeatherProviderInterface {
    private final LinkedHashMap<CityID, WeatherEntity> weathers;
    private final EventBus bus;

    public OpenWeatherOrgProvider() {
        bus = new EventBus();
        weathers = new LinkedHashMap<>();
    }

    public final EventBus getBus() {
        return bus;
    }

    @Nullable
    @Override
    public WeatherEntity getWeatherFor(CityID city) {
        synchronized (weathers) {
            return weathers.get(city);
        }
    }

    @Nullable
    @Override
    public ArrayList<WeatherEntity> getWeatherWeekForecastFor(CityID city) {
        return new ArrayList<>();
    }

    public void findForecastFor(String keywords) {
        try {
            String jsonData = OpenWeatherOrgRequest.getWeatherSearchResponseFor(keywords);
            Gson gson = new Gson();
            final FindData findData = gson.fromJson(jsonData, FindData.class);

            if ( findData.getCod() == 200 ) { //Request completed
                //Build found list
                LinkedList<OpenWeatherSearchResultEvent.WeatherSearchDetails> found = new LinkedList<>();
                for ( FindWeatherData wd : findData.getList() ) {
                    found.add(new OpenWeatherSearchResultEvent.WeatherSearchDetails(wd.getId(),
                            wd.getName(),
                            wd.getCoord(),
                            wd.getSys().getCountry()));
                }
                bus.post(new OpenWeatherSearchResultEvent(OpenWeatherProviderEvent.OWeatherResult.REQUEST_COMPLETED,
                        found, keywords));
            } else { //City not found or some other error
                bus.post(new OpenWeatherSearchResultEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, findData.getMessage(), keywords));
            }
        } catch (IOException e) {
            bus.post(new OpenWeatherSearchResultEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, "Connection error", keywords));
        } catch (Exception e) {
            bus.post(new OpenWeatherSearchResultEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, "Internal error", keywords));
        }
    }

    public void updateWeatherFor(final CityID city) {
        WeatherEntity updatedWeather = null;
        try {
            synchronized (weathers) {
                weathers.put(city, null);
            }
            String jsonData = OpenWeatherOrgRequest.getWeatherUpdateResponseFor(city);
            Gson gson = new Gson();
            final WeatherData weatherData = gson.fromJson(jsonData, WeatherData.class);

            if ( weatherData.getCode() == 200 ) { //City data processing
                updatedWeather = new WeatherEntity(weatherData); //Mapping
                synchronized (weathers) {
                    weathers.put(city, updatedWeather);
                    bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.REQUEST_COMPLETED, city));
                }
            } else { //City not found or some other error
                bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, city, weatherData.getErrorMessage()));
            }
        } catch (IOException e) {
            bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, city, "Connection error"));
        } catch (Exception e) {
            bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, city, "Internal error"));
        }
    }

    public void refreshWeatherList() {
        CityID[] cityList;
        synchronized (weathers) {
            cityList = new CityID[weathers.keySet().size()];
            weathers.keySet().toArray(cityList);
        }

        for ( CityID city : cityList ) {
            updateWeatherFor(city);
        }
    }

    public static int convertWeatherImageID2Custom(String oweatherID) {
        switch (oweatherID) {
            case "01d":
                return R.mipmap.ic_clear_sky_day;
            case "01n":
                return R.mipmap.ic_clear_sky_night;
            case "02d":
                return R.mipmap.ic_few_clouds_day;
            case "02n":
                return R.mipmap.ic_few_clouds_night;
            case "03d":
            case "03n":
                return R.mipmap.ic_scattered_clouds_d_n;
            case "04d":
            case "04n":
                return R.mipmap.ic_broken_clouds_d_n;
            case "09d":
            case "09n":
                return R.mipmap.ic_rain_d_n;
            case "10d":
                return R.mipmap.ic_rain_day;
            case "10n":
                return R.mipmap.ic_rain_night;
            case "11d":
            case "11n":
                return R.mipmap.ic_thunder_strom;
            case "13d":
            case "13n":
                return R.mipmap.ic_snow_d_n;
            default:
                return R.mipmap.ic_weather_na;
        }
    }
}
