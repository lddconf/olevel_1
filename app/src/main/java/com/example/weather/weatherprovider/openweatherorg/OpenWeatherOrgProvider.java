package com.example.weather.weatherprovider.openweatherorg;

import androidx.annotation.Nullable;

import com.example.weather.CityID;
import com.example.weather.R;
import com.example.weather.weather.WeatherEntity;
import com.example.weather.weatherprovider.OpenWeatherOrgRetrofitServices;
import com.example.weather.weatherprovider.WeatherProviderInterface;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;

import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.WeatherData;
import com.example.weather.weatherprovider.openweatherorg.findWeatherModel.FindData;
import com.example.weather.weatherprovider.openweatherorg.findWeatherModel.FindWeatherData;

import org.greenrobot.eventbus.EventBus;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OpenWeatherOrgProvider implements WeatherProviderInterface {
    private final LinkedHashMap<CityID, WeatherEntity> weathers;
    private final EventBus bus;
    private OpenWeatherOrgRetrofitServices openWeather;
    private static final String WEATHER_API_KEY = "bb0dbb13a7f84df1ca260fe5fcab1320";
    private static final String WEATHER_UNITS = "metric";

    public OpenWeatherOrgProvider() {
        bus = new EventBus();
        weathers = new LinkedHashMap<>();
        initRetrofit();
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

    private void initRetrofit() {
        Retrofit retrofit;
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .client(getUnsafeOkHttpClient().build())
                .addConverterFactory(GsonConverterFactory.create()).build();

        openWeather = retrofit.create(OpenWeatherOrgRetrofitServices.class);
    }

    private void parseFindData( final String keywords, final FindData findData, boolean isCoord ) {
        if ( findData.getCod() == 200 ) { //Request completed
            //Build found list
            LinkedList<OpenWeatherSearchResultEvent.WeatherSearchDetails> found = new LinkedList<>();
            for ( FindWeatherData wd : findData.getList() ) {
                found.add(new OpenWeatherSearchResultEvent.WeatherSearchDetails(wd.getId(),
                        wd.getName(),
                        wd.getCoord(),
                        wd.getSys().getCountry()));
            }
            bus.post(new OpenWeatherSearchResultEvent(isCoord ? OpenWeatherProviderEvent.OWeatherResult.REQUEST_COMPLETED_LAT_LONG
                    : OpenWeatherProviderEvent.OWeatherResult.REQUEST_COMPLETED_KEYWORD,
                    found, keywords));
        } else { //City not found or some other error
            bus.post(new OpenWeatherSearchResultEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, findData.getMessage(), keywords));
        }
    }

    private static String formatKeywordsForFindRequest(final String keywords) {
        if (keywords.length() < 3) {
            return String.format(Locale.getDefault(), "%-3.3s", keywords);
        }
        return keywords;
    }

    public void findForecastForSync(String keywords) {
        try {
            String reqKey = formatKeywordsForFindRequest(keywords);

            //Request data
            Response<FindData> response = openWeather.findWeatherFor(reqKey, WEATHER_UNITS, WEATHER_API_KEY).execute();
            if ( response.body() == null ) {
                throw new IOException("Empy body");
            }
            if ( response.code() != 200 ) {
                throw new IOException("Response code not 200");
            }
            final FindData findData = response.body();

            parseFindData(keywords, findData, false);
        } catch (IOException e) {
            bus.post(new OpenWeatherSearchResultEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, "Connection error", keywords));
        } catch (Exception e) {
            bus.post(new OpenWeatherSearchResultEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, "Internal error", keywords));
        }
    }

    public void findForecastForAsync(String keywords) {
        String reqKey = formatKeywordsForFindRequest(keywords);

        //Request data
        openWeather.findWeatherFor(reqKey, WEATHER_UNITS, WEATHER_API_KEY).enqueue(new Callback<FindData>() {
            @Override
            public void onResponse(Call<FindData> call, Response<FindData> response) {
                if (response.body() == null) {
                    bus.post(new OpenWeatherSearchResultEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, "Empty server reply", keywords));
                    return;
                }
                if (response.code() != 200) {
                    bus.post(new OpenWeatherSearchResultEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, "Response code not 200", keywords));
                    return;
                }
                final FindData findData = response.body();
                parseFindData(keywords, findData, false);
            }

            @Override
            public void onFailure(Call<FindData> call, Throwable t) {
                bus.post(new OpenWeatherSearchResultEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, "Connection error", keywords));
            }
        });
    }

    public void findForecastForAsync(double lat, double lon) {
        String keywords = String.format(Locale.getDefault(), "lat=%.3f,lon=%.3f", lat, lon);
        //Request data
        openWeather.findWeatherForLatLong(lat, lon, WEATHER_UNITS, 1, WEATHER_API_KEY).enqueue(new Callback<FindData>() {
            @Override
            public void onResponse(Call<FindData> call, Response<FindData> response) {

                if (response.body() == null) {
                    bus.post(new OpenWeatherSearchResultEvent(
                            OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR,
                            "Empty server reply", keywords ));
                    return;
                }
                if (response.code() != 200) {
                    bus.post(new OpenWeatherSearchResultEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, "Response code not 200", keywords));
                    return;
                }
                final FindData findData = response.body();
                parseFindData(keywords, findData, true);
            }

            @Override
            public void onFailure(Call<FindData> call, Throwable t) {
                bus.post(new OpenWeatherSearchResultEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, "Connection error", keywords));
            }
        });
    }

    public void updateWeatherForSync(final CityID city) {
        WeatherEntity updatedWeather = null;
        try {
            synchronized (weathers) {
                weathers.put(city, null);
            }

            //Request data
            Response<WeatherData> response = openWeather.getWeatherByID(city.getId(), WEATHER_UNITS, WEATHER_API_KEY).execute();
            if ( response.body() == null ) {
                throw new IOException("Empy body");
            }
            if ( response.code() != 200 ) {
                throw new IOException("Response code not 200");
            }
            final WeatherData weatherData = response.body();

            if ( weatherData.getCode() == 200 ) { //City data processing
                updatedWeather = new WeatherEntity(weatherData); //Mapping
                synchronized (weathers) {
                    weathers.put(city, updatedWeather);
                }
                bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.REQUEST_COMPLETED_KEYWORD, city));
            } else { //City not found or some other error
                bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, city, weatherData.getErrorMessage()));
            }
        } catch (IOException e) {
            bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, city, "Connection error"));
        } catch (Exception e) {
            bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, city, "Internal error"));
        }
    }

    public void updateWeatherForAsync(final CityID city) {
        if ( city == null ) return;
        synchronized (weathers) {
            weathers.put(city, null);
        }
        openWeather.getWeatherByID(city.getId(), WEATHER_UNITS, WEATHER_API_KEY).enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                WeatherEntity updatedWeather = null;
                if ( response.body() == null ) {
                    bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, city, "Empty server reply"));
                    return;
                }
                if ( response.code() != 200 ) {
                    bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, city, "Response code not 200"));
                }

                final WeatherData weatherData = response.body();
                if ( weatherData.getCode() == 200 ) { //City data processing
                    updatedWeather = new WeatherEntity(weatherData); //Mapping
                    synchronized (weathers) {
                        weathers.put(city, updatedWeather);
                    }
                    bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.REQUEST_COMPLETED_KEYWORD, city));
                } else { //City not found or some other error
                    bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, city, weatherData.getErrorMessage()));
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                bus.post(new OpenWeatherProviderEvent(OpenWeatherProviderEvent.OWeatherResult.CONNECTION_ERROR, city, "Connection error"));
            }
        });
    }

    public void refreshWeatherListSync() {
        CityID[] cityList;
        synchronized (weathers) {
            cityList = new CityID[weathers.keySet().size()];
            weathers.keySet().toArray(cityList);
        }

        for ( CityID city : cityList ) {
            updateWeatherForSync(city);
        }
    }

    public void refreshWeatherListAsync() {
        CityID[] cityList;
        synchronized (weathers) {
            cityList = new CityID[weathers.keySet().size()];
            weathers.keySet().toArray(cityList);
        }

        for ( CityID city : cityList ) {
            updateWeatherForAsync(city);
        }
    }

    public static int convertWeatherImageID2BuildIn(String oweatherID) {
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

    public static OkHttpClient.Builder getUnsafeOkHttpClient() {

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
