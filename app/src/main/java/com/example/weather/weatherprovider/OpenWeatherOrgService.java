package com.example.weather.weatherprovider;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.example.weather.CityID;
import com.example.weather.weather.WeatherEntity;
import com.example.weather.weatherprovider.openweatherorg.OpenWeatherOrgProvider;
import com.example.weather.weatherprovider.openweatherorg.OpenWeatherProviderEvent;
import com.example.weather.weatherprovider.openweatherorg.OpenWeatherSearchResultEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class OpenWeatherOrgService extends Service {
    private final IBinder binder = new OpenWeatherOrgBinder();
    private final OpenWeatherOrgProvider provider = new OpenWeatherOrgProvider();

    public static final String BROADCAST_ACTION_SEARCH_FINISHED = "com.example.weather.weatherprovider.openweatherorg.OpenWeatherSearchResultEvent";
    public static final String BROADCAST_ACTION_SEARCH_RESULT = "com.example.weather.weatherprovider.openweatherorg.OpenWeatherSearchResultEvent.RESULT";

    public static final String BROADCAST_ACTION_WEATHER_UPDATE_FINISHED = "com.example.weather.weatherprovider.openweatherorg.OpenWeatherProviderEvent";
    public static final String BROADCAST_ACTION_WEATHER_UPDATE_RESULT = "com.example.weather.weatherprovider.openweatherorg.OpenWeatherProviderEvent.RESULT";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        provider.getBus().register(binder);
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        provider.getBus().unregister(binder);
        return super.onUnbind(intent);
    }

    class OpenWeatherOrgBinder extends Binder {

        public OpenWeatherOrgService getService() {
            return OpenWeatherOrgService.this;
        }

        /**
         * Find weather by keywords
         * @param keyword - searched place
         */
        public void findForecastFor(String keyword) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    provider.findForecastFor(keyword);
                }
            }).start();
        }

        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onOWeatherEvent(OpenWeatherProviderEvent event) {
            Intent broadcastIntent = new Intent(BROADCAST_ACTION_WEATHER_UPDATE_FINISHED);
            broadcastIntent.putExtra(BROADCAST_ACTION_WEATHER_UPDATE_RESULT, event);
            sendBroadcast(broadcastIntent);
        }

        //Handler for city search result
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onFoundCities(OpenWeatherSearchResultEvent event) {
            Intent broadcastIntent = new Intent(BROADCAST_ACTION_SEARCH_FINISHED);
            broadcastIntent.putExtra(BROADCAST_ACTION_SEARCH_RESULT, event);
            sendBroadcast(broadcastIntent);
        }

        public ArrayList<WeatherEntity> getWeatherWeekForecastFor(CityID city) {
            return provider.getWeatherWeekForecastFor(city);
        }

        public WeatherEntity getWeatherFor(CityID city) {
            return provider.getWeatherFor(city);
        }

        private void refreshWeatherDataFor(CityID city) {
            //Request new weather data from internet
            new Thread(new Runnable() {
                @Override
                public void run() {
                    provider.updateWeatherFor(city);
                }
            }).start();
        }

        public void refreshWeatherList() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    provider.refreshWeatherList();
                }
            }).start();

        }
    }
}
