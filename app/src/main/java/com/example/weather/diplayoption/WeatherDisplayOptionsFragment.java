package com.example.weather.diplayoption;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.weather.R;

public class WeatherDisplayOptionsFragment extends Fragment {
    private SwitchCompat showWindSwitch;
    private SwitchCompat showPressure;
    private SwitchCompat showFeelsLike;
    private SwitchCompat temperatureUnit;
    private SwitchCompat themeSelection;
    private WeatherDisplayOptions settings;
    private ThemeChanged themeChangedCallback;

    public static final String DisplayOptionsKey = "DisplayOptionsKey";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        settings = new WeatherDisplayOptions();
        setRetainInstance(true);
        if ( getArguments() != null ) {
            WeatherDisplayOptions options = (WeatherDisplayOptions)getArguments().getSerializable(DisplayOptionsKey);
            if ( options != null ) {
                settings = options;
            }
        }
        return inflater.inflate(R.layout.diplay_options_frame, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if ( savedInstanceState != null ) {
            settings = (WeatherDisplayOptions)savedInstanceState.getSerializable(DisplayOptionsKey);
        }
        findViews(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupTemperatureUnit();
        setupWindSwitch();
        setupPressureSwitch();
        setupFeelsLikeSwitch();


        updateShowWindSpeed();
        updateShowPressure();
        updateShowFeelsLike();

        updateThemeSelection();
        setupThemeSelection();
    }

    private void setupTemperatureUnit() {
        temperatureUnit.setChecked(settings.isFahrenheitTempUnit());
        updateTemperatureUnit();
        temperatureUnit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTemperatureUnit();
            settings.setTemperatureUnit(isChecked);
        });
    }

    private void setupWindSwitch() {
        showWindSwitch.setOnCheckedChangeListener((compoundButton, b) -> settings.setShowWindSpeed(b));
    }

    private void setupThemeSelection() {
        themeSelection.setOnCheckedChangeListener((compoundButton, b) -> {
            if ( b ) {
                settings.setThemeDark();
            } else {
                settings.setThemeLight();
            }
            if ( themeChangedCallback != null ) {
                themeChangedCallback.onThemeChanged();
            }
        });
    }
    private void setupPressureSwitch() {
        showPressure.setOnCheckedChangeListener((compoundButton, b) -> settings.setShowPressure(b));
    }

    private void updateThemeSelection() {
        themeSelection.setChecked(!settings.isLightTheme());

    }

    private void setupFeelsLikeSwitch() {
        showFeelsLike.setOnCheckedChangeListener((compoundButton, b) -> settings.setShowFeelsLike(b));
    }

    private void updateShowWindSpeed() {
        showWindSwitch.setChecked(settings.isShowWindSpeed());
    }

    private void updateShowPressure() {
        showPressure.setChecked(settings.isShowPressure());
    }

    private void updateShowFeelsLike() {
        showFeelsLike.setChecked(settings.isShowFeelsLike());
    }

    public WeatherDisplayOptions getCurrentOptions() {
        return settings;
    }

    public interface ThemeChanged {
        void onThemeChanged();
    }

    public void setOnThemeChangedListener(ThemeChanged call) {
        themeChangedCallback = call;
    }

    private void updateTemperatureUnit() {
        String displayText = getString(R.string.temperature_unit_title);
        if ( temperatureUnit.isChecked() ) { //Means Fahrenheit
            displayText += " (" + getString(R.string.temp_unit_fahrenheit) + ") ";
        } else {
            displayText += " (" + getString(R.string.temp_unit_celsius) + ") ";
        }
        temperatureUnit.setText(displayText.toCharArray(),0, displayText.length());
    }

    private void findViews(@NonNull View view) {
        showFeelsLike = view.findViewById(R.id.enableFeelsLike);
        showWindSwitch = view.findViewById(R.id.enableWindView);
        showPressure = view.findViewById(R.id.enablePressure);
        temperatureUnit = view.findViewById(R.id.temperatureUnit);
        themeSelection = view.findViewById(R.id.themeSelection);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //Реализация сделана по условиям ДЗ. Понятно, что фактически, ничего специально сохранять не надо
        if ( settings != null ) {
            settings.setShowPressure(showPressure.isChecked());
            settings.setShowWindSpeed(showWindSwitch.isChecked());
            settings.setTemperatureUnit(temperatureUnit.isChecked());
            settings.setShowFeelsLike(showFeelsLike.isChecked());
            outState.putSerializable(DisplayOptionsKey, settings);
        }


        super.onSaveInstanceState(outState);
    }
}
