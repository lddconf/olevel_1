package com.example.weather;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weather.weather.SimpleWeatherProvider;
import com.example.weather.weather.WeatherProviderInterface;


/**
 * Weather ac
 */
public class WeatherSettingsActivity extends AppCompatActivity {
    private Button okButton;
    private Button cancelButton;
    private Spinner citySpinner;
    private Switch showWindSwitch;
    private Switch showPressure;
    private Switch showFeelsLike;
    private Switch temperatureUnit;
    private WeatherSettingsActivityCurrentStatus settings;

    private final String weatherSettingsActivityKey = "WeatherSettingsActivityKey";
    private static WeatherProviderInterface weatherProvider = new SimpleWeatherProvider();

    private static final boolean debug = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        settings = (WeatherSettingsActivityCurrentStatus)getIntent().getSerializableExtra(MainActivity.mainActivityViewOptionsKey);

        findViews();
        setupOkButtonOnClickListener();
        setupCancelButtonOnClickListener();
        setupCityListSpinner();
        setupTemperatureUnit();
        setupWindSwitch();
        setupPressureSwitch();
        setupFeelsLikeSwitch();

        updateShowWindSpeed();
        updateShowPressure();
        updateShowFeelsLike();



        onDebug("onCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDebug("onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();
        onDebug("onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        onDebug("onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        onDebug("onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        onDebug("onResume");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        //Реализация сделана по условиям ДЗ. Понятно, что фактически, ничего специально сохранять не надо
        settings.setCity(citySpinner.getSelectedItem().toString());
        settings.setShowFeelsLike(showFeelsLike.isChecked());
        settings.setShowPressure(showPressure.isChecked());
        settings.setShowWindSpeed(showWindSwitch.isChecked());
        settings.setTemperatureUnit(temperatureUnit.isChecked());
        outState.putSerializable(weatherSettingsActivityKey, settings);

        onDebug("onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //Реализация сделана по условиям ДЗ. Понятно, что фактически, ничего восстанавливать не надо
        WeatherSettingsActivityCurrentStatus savedSettings = (WeatherSettingsActivityCurrentStatus)savedInstanceState.getSerializable(weatherSettingsActivityKey);
        if ( savedSettings != null ) {
            settings = savedSettings;
            showWindSwitch.setChecked(settings.isShowWindSpeed());
            showPressure.setChecked(settings.isShowPressure());
            showFeelsLike.setChecked(settings.isShowFeelsLike());
            temperatureUnit.setChecked(settings.isFahrenheitTempUnit());

            //Restore spinner value
            for ( int i = 0; i < citySpinner.getCount(); i++ ) {
                if ( citySpinner.getItemAtPosition(i).toString().equals(settings.getCity()) ) {
                    citySpinner.setSelection(i);
                }
            }
        }

        onDebug("savedInstanceState");
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
                Intent appliedSettings = new Intent();
                appliedSettings.putExtra(MainActivity.mainActivityViewOptionsKey, settings);
                setResult(RESULT_OK, appliedSettings);
                finish();
            }
        });
    }

    private void setupCancelButtonOnClickListener() {
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void setupCityListSpinner() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.settings_spinner_item, weatherProvider.getCitiesList());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(arrayAdapter);
        citySpinner.setSelection(arrayAdapter.getPosition(settings.getCity()));
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                settings.setCity(adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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

    /**
     * Debug purposes
     * @param textToPrint debug text to print
     */
    private void onDebug(String textToPrint) {
        if ( debug ) {
            String weatherSettingsActivityTAG = "WeatherSettingsActivity";
            Log.d(weatherSettingsActivityTAG, textToPrint);
            Toast.makeText(getApplicationContext(), textToPrint, Toast.LENGTH_SHORT).show();
        }
    }
}


