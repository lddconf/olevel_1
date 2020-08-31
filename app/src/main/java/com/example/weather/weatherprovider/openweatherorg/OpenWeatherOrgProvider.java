package com.example.weather.weatherprovider.openweatherorg;

import androidx.annotation.Nullable;

import com.example.weather.weather.WeatherEntity;
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
import java.util.Arrays;
import java.util.LinkedHashMap;
import android.os.Handler;

import com.example.weather.weatherprovider.openweatherorg.model.WeatherData;
import com.google.gson.Gson;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class OpenWeatherOrgProvider implements WeatherProviderInterface {
    private static final String WEATHER_URL_BEFORE_CITY = "https://api.openweathermap.org/data/2.5/weather?q=";
    private static final String WEATHER_URL_AFTER_CITY = "&units=metric&appid=";
    private static final String WEATHER_API_KEY = "bb0dbb13a7f84df1ca260fe5fcab1320";

    private WeatherOnUpdateErrorListener errorListener;
    private WeatherUpdatedListener updateListener;

    private final LinkedHashMap<String, WeatherEntity> weathers;

    public OpenWeatherOrgProvider() {
        errorListener = null;
        updateListener = null;

        weathers = new LinkedHashMap<>();
        String[] cities = new String[] {"Moscow", "New York", "Berlin", "Paris", "Prague", "Minsk"};
        Arrays.sort(cities);

        for ( String key : cities ) {
            weathers.put(key, null);
        }
    }

    @Override
    public String[] getCitiesList() {
        synchronized (weathers) {
            String[] cities = new String[weathers.keySet().size()];
            return weathers.keySet().toArray(cities);
        }
    }

    @Nullable
    @Override
    public WeatherEntity getWeatherFor(String city) {
        synchronized (weathers) {
            return weathers.get(city);
        }
    }

    @Nullable
    @Override
    public ArrayList<WeatherEntity> getWeatherWeekForecastFor(String city) {
        return new ArrayList<>();
    }

    public interface WeatherOnUpdateErrorListener {
        void onErrorOccurredNotify(String error);
    }

    public interface WeatherUpdatedListener {
        void onWeatherUpdatedNotify(String city);
    }

    synchronized
    public void setErrorListener(WeatherOnUpdateErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    synchronized
    public void setUpdateListener(WeatherUpdatedListener updateListener) {
        this.updateListener = updateListener;
    }

    synchronized
    private void onErrorNotify(String error) {
        if ( errorListener != null ) {
            errorListener.onErrorOccurredNotify(error);
        }
    }

    synchronized
    private void onWeatherUpdateNotify(String city) {
        if ( updateListener != null ) {
            updateListener.onWeatherUpdatedNotify(city);
        }
    }

    /** Были проблемы с сертификаторм.
     * https://stackoverflow.com/questions/35548162/how-to-bypass-ssl-certificate-validation-in-android-app
     * Disables the SSL certificate checking for new instances of {@link HttpsURLConnection} This has been created to
     * aid testing on a local box, not for use on production.
     */
    public static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() { @Override public boolean verify(String hostname, SSLSession session) { return true; } });
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void updateWeatherFor(final String city) {
        HttpsURLConnection urlConnection = null;
        WeatherEntity updatedWeather = null;
        disableSSLCertificateChecking();
        try {
            final URL url = new URL(WEATHER_URL_BEFORE_CITY + city + WEATHER_URL_AFTER_CITY + WEATHER_API_KEY);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(1000);
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String jsonData = getLines(in);
            Gson gson = new Gson();
            final WeatherData weatherData = gson.fromJson(jsonData, WeatherData.class);

            if ( weatherData.getCode() == 200 ) { //City data processing
                updatedWeather = weatherData.toWeatherEntity();
                synchronized (weathers) {
                    weathers.put(city, updatedWeather);
                }
            } else { //City not found or some other error
                onErrorNotify(weatherData.getErrorMessage());
            }
        } catch (IOException e) {
            onErrorNotify("Connection error");
        } catch (Exception e) {
            onErrorNotify("Internal error");
        } finally {
            if ( urlConnection != null ) {
                urlConnection.disconnect();
            }
        }
    }

    public void updateWeatherList() {
        String[] cityList;
        synchronized (weathers) {
            cityList = new String[weathers.keySet().size()];
            weathers.keySet().toArray(cityList);
        }

        for ( String city : cityList ) {
            updateWeatherFor(city);
            onWeatherUpdateNotify(city);
        }
        onWeatherUpdateNotify(null);
    }

    private String getLines(final BufferedReader reader) throws IOException {
        StringBuilder builder = new StringBuilder(1580);
        String line = null;
        do {
            line = reader.readLine();
            if ( line != null) {
                builder.append(line);
            }
        } while (line != null);
        return builder.toString();
    }
}
