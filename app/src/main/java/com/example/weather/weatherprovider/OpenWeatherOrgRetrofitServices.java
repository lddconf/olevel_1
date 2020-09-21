package com.example.weather.weatherprovider;

import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.WeatherData;
import com.example.weather.weatherprovider.openweatherorg.findWeatherModel.FindData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenWeatherOrgRetrofitServices {
    @GET("data/2.5/weather")
    Call<WeatherData> getWeatherByID(@Query("id") int cityID, @Query("units") String units, @Query("appid") String keyApi);

    @GET("data/2.5/find")
    Call<FindData> findWeatherFor(@Query("q") String keyword, @Query("units") String units, @Query("appid") String keyApi);

    @GET("data/2.5/find")
    Call<FindData> findWeatherForLatLong(@Query("lat") double latitude, @Query("lon") double longitude, @Query("units") String units, @Query("cnt") int count, @Query("appid") String keyApi);
}