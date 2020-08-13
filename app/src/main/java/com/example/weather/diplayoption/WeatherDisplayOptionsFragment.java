package com.example.weather.diplayoption;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weather.R;

public class WeatherDisplayOptionsFragment extends Fragment {
    private Switch showWindSwitch;
    private Switch showPressure;
    private Switch showFeelsLike;
    private Switch temperatureUnit;
    private WeatherDisplayOptions settings;

    public static final String DisplayOptionsKey = "DisplayOptionsKey";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        settings = new WeatherDisplayOptions();
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
    }

    private void setupTemperatureUnit() {
        temperatureUnit.setChecked(settings.isFahrenheitTempUnit());
        updateTemperatureUnit();
        temperatureUnit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateTemperatureUnit();
                settings.setTemperatureUnit(isChecked);
            }
        });
    }

    private void setupWindSwitch() {
        showWindSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setShowWindSpeed(b);
            }
        });
    }

    private void setupPressureSwitch() {
        showPressure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setShowPressure(b);
            }
        });
    }

    private void setupFeelsLikeSwitch() {
        showFeelsLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setShowFeelsLike(b);
            }
        });
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
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //Реализация сделана по условиям ДЗ. Понятно, что фактически, ничего специально сохранять не надо
        settings.setShowFeelsLike(showFeelsLike.isChecked());
        settings.setShowPressure(showPressure.isChecked());
        settings.setShowWindSpeed(showWindSwitch.isChecked());
        settings.setTemperatureUnit(temperatureUnit.isChecked());

        outState.putSerializable(DisplayOptionsKey, settings);
        super.onSaveInstanceState(outState);
    }
}
