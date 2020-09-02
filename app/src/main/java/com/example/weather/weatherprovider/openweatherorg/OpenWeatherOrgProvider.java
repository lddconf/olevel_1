package com.example.weather.weatherprovider.openweatherorg;

import androidx.annotation.Nullable;

import com.example.weather.CityID;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;

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
    private static final String WEATHER_URL_BEFORE_CURRENT_WEATHER_REQUEST = "https://api.openweathermap.org/data/2.5/weather?id=";
    private static final String WEATHER_URL_BEFORE_FIND_REQUEST = "https://api.openweathermap.org/data/2.5/find?q=";
    private static final String WEATHER_URL_AFTER_CITY = "&units=metric&appid=";
    private static final String WEATHER_API_KEY = "bb0dbb13a7f84df1ca260fe5fcab1320";

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

    public void findForecastFor(final String keywords) {
        HttpsURLConnection urlConnection = null;
        WeatherEntity updatedWeather = null;
        disableSSLCertificateChecking();
        try {
            final URL url = new URL(WEATHER_URL_BEFORE_FIND_REQUEST
                    + keywords + WEATHER_URL_AFTER_CITY
                    + WEATHER_API_KEY );
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(1000);
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String jsonData = getLines(in);
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
        } finally {
            if ( urlConnection != null ) {
                urlConnection.disconnect();
            }
        }
    }

    public void updateWeatherFor(final CityID city) {
        HttpsURLConnection urlConnection = null;
        WeatherEntity updatedWeather = null;
        disableSSLCertificateChecking();
        try {
            synchronized (weathers) {
                weathers.put(city, null);
            }
            final URL url = new URL(WEATHER_URL_BEFORE_CURRENT_WEATHER_REQUEST
                    + city.getId() + WEATHER_URL_AFTER_CITY
                    + WEATHER_API_KEY );
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
                    bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.REQUEST_COMPLETED, city));
                }
            } else { //City not found or some other error
                bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, city, weatherData.getErrorMessage()));
            }
        } catch (IOException e) {
            bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, city, "Connection error"));
        } catch (Exception e) {
            bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, city, "Internal error"));
        } finally {
            if ( urlConnection != null ) {
                urlConnection.disconnect();
            }
        }
    }

    public void updateWeatherList() {
        CityID[] cityList;
        synchronized (weathers) {
            cityList = new CityID[weathers.keySet().size()];
            weathers.keySet().toArray(cityList);
        }

        for ( CityID city : cityList ) {
            updateWeatherFor(city);
        }
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
