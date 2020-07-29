package com.example.weather;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.example.weather.weather.SimpleWeatherProvider;
import com.example.weather.weather.WeatherProviderInterface;

import java.lang.reflect.Array;
import java.util.Arrays;

public class WeatherSettingsActivity extends AppCompatActivity {
    private Button    okButton;
    private Button    cancelButton;
    private Spinner   citySpinner;
    private Switch    showWindSwitch;
    private Switch    showPressure;
    private Switch    showFeelsLike;
    private Switch    temperatureUnit;
    private static WeatherProviderInterface weatherProvider;

    static {
        weatherProvider = new SimpleWeatherProvider();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        findViews();
        setupOkButtonOnClickListener();
        setupCancelButtonOnClickListener();
        setupCityListSpinner();
        setupTemperatureUnit();
    }

    private void setupTemperatureUnit() {
        updateTemperatureUnit();
        temperatureUnit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateTemperatureUnit();
            }
        });
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

    private void setupOkButtonOnClickListener() {
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupCancelButtonOnClickListener() {
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupCityListSpinner() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.settings_spinner_item, weatherProvider.getCitiesList());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(arrayAdapter);
    }

    private void findViews() {
        okButton = findViewById(R.id.okButton);
        cancelButton = findViewById(R.id.cancelButton);
        citySpinner = findViewById(R.id.citySelection);
        showFeelsLike = findViewById(R.id.enableFeelsLike);
        showWindSwitch = findViewById(R.id.enableWindView);
        showPressure = findViewById(R.id.enablePressure);
        temperatureUnit = findViewById(R.id.temperatureUnit);
    }

}