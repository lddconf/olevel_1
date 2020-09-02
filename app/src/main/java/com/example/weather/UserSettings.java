package com.example.weather;

import androidx.annotation.Nullable;

import com.example.weather.diplayoption.WeatherDisplayOptions;

import java.util.Arrays;
import java.util.LinkedHashSet;

public class UserSettings {
    private CityID currentPlace;
    private LinkedHashSet<CityID> otherPlaces;
    private String username;
    private int avatarID;
    private WeatherDisplayOptions options;

    private static UserSettings settings;

    public static class CityID {
        private String name;
        private String country;
        private int id;

        public CityID(String name, String country, int id) {
            this.name = name;
            this.country = country;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public String getCountry() {
            return country;
        }
    };

    private UserSettings() {
        currentPlace = new CityID("Moscow", "RU", 524901);
        otherPlaces = new LinkedHashSet<>();

        otherPlaces.add(new CityID("New York", "US", 5128638));
        otherPlaces.add(new CityID("Berlin", "DE", 2950159));

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

    public CityID getCurrentPlace() {
        return currentPlace;
    }

    public CityID[] getOtherPacesList() {
        CityID[] result = new CityID[otherPlaces.size()];
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

    public void setCurrentPlace(CityID currentPlace) {
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

    public void addOtherPlaces(CityID[] places ) {
        this.otherPlaces.addAll(Arrays.asList(places));

    }

    public void removeOtherPlaces(CityID[] places ) {
        this.otherPlaces.removeAll(Arrays.asList(places));
        if ( !this.otherPlaces.contains(currentPlace) ) {
            currentPlace = null;
        }
    }
}
