package com.example.weather;

import com.example.weather.diplayoption.WeatherDisplayOptions;

import java.util.Arrays;
import java.util.LinkedHashSet;

public class UserSettings {
    private String currentPlace;
    private LinkedHashSet<String> otherPlaces;
    private String username;
    private int avatarID;
    private WeatherDisplayOptions options;

    private static UserSettings settings;

    private UserSettings() {
        currentPlace = "Moscow";
        otherPlaces = new LinkedHashSet<>();

        String[] cities = new String[] {"New York", "Berlin", "Paris", "Prague", "Minsk"};

        otherPlaces.addAll(Arrays.asList(cities));

        username = "User";
        avatarID = R.mipmap.ic_launcher_round;

        options = new WeatherDisplayOptions();
        options.setTemperatureUnit(false);
    }

    public static UserSettings getUserSettings() {
        if ( settings == null ) {
            settings = new UserSettings();
        }
        return settings;
    }

    public String getCurrentPlace() {
        return currentPlace;
    }

    public String[] getOtherPacesList() {
        String[] result = new String[otherPlaces.size()];
        otherPlaces.toArray(result);
        return result;
    }

    public String getUsername() {
        return username;
    }

    public int getAvatarID() {
        return avatarID;
    }

    public WeatherDisplayOptions getOptions() {
        return options;
    }

    public void setCurrentPlace(String currentPlace) {
        this.currentPlace = currentPlace;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setOptions(WeatherDisplayOptions options) {
        this.options = options;
    }

    public void removeAllOtherPlaces() {
        otherPlaces.clear();
    }

    public void addOtherPlaces(String[] places ) {
        this.otherPlaces.addAll(Arrays.asList(places));

    }

    public void removeOtherPlaces(String[] places ) {
        this.otherPlaces.removeAll(Arrays.asList(places));
        if ( !this.otherPlaces.contains(currentPlace) ) {
            currentPlace = null;
        }
    }
}
