package com.example.weather.weatherprovider.openweatherorg;

import com.example.weather.CityID;
import com.example.weather.weatherprovider.openweatherorg.currentWeatherModel.Coord;

import java.io.Serializable;
import java.util.LinkedList;

public class OpenWeatherSearchResultEvent {
    OpenWeatherProviderEvent.OWeatherResult resultCode;
    LinkedList<WeatherSearchDetails> searchDetails;
    String keyword;
    String errorDescription;

    public static class WeatherSearchDetails implements Serializable {
        private CityID cityID;
        private Coord coord;

        public WeatherSearchDetails(int id, String name, Coord coord, String country) {
            cityID = new CityID(name, country, id);
            this.coord = coord;
        }

        public int getId() {
            return cityID.getId();
        }

        public String getName() {
            return cityID.getName();
        }

        public Coord getCoord() {
            return coord;
        }

        public String getCountry() {
            return cityID.getCountry();
        }

        public CityID getCityID() {
            return cityID;
        }
    }

    public OpenWeatherSearchResultEvent(OpenWeatherProviderEvent.OWeatherResult resultCode, LinkedList<WeatherSearchDetails> searchDetails, String keyword) {
        this.resultCode = resultCode;
        this.searchDetails = searchDetails;
        this.errorDescription = null;
        this.keyword = keyword;
    }

    public OpenWeatherSearchResultEvent(OpenWeatherProviderEvent.OWeatherResult resultCode, String errorDescription, String keyword) {
        this.resultCode = resultCode;
        this.errorDescription = errorDescription;
        this.searchDetails = null;
        this.keyword = keyword;
    }

    public OpenWeatherProviderEvent.OWeatherResult getResultCode() {
        return resultCode;
    }

    public LinkedList<WeatherSearchDetails> getSearchDetails() {
        return searchDetails;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getKeyword() {
        return keyword;
    }
}
