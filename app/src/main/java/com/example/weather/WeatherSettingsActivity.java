package com.example.weather;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.weather.weather.SimpleWeatherProvider;
import com.example.weather.weather.WeatherProviderInterface;


import java.util.Objects;


/**
 * Weather ac
 */
public class WeatherSettingsActivity extends AppCompatActivity {
    private Spinner citySpinner;
    private Switch showWindSwitch;
    private Switch showPressure;
    private Switch showFeelsLike;
    private Switch temperatureUnit;
    private WeatherSettingsActivityCurrentStatus settings;
    private Toolbar headToolBar;
    private final String weatherSettingsActivityKey = "WeatherSettingsActivityKey";
    private static WeatherProviderInterface weatherProvider = new SimpleWeatherProvider();

    private static final boolean debug = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        settings = (WeatherSettingsActivityCurrentStatus)getIntent().getSerializableExtra(MainActivity.mainActivityViewOptionsKey);

        findViews();
        setupCityListSpinner();
        setupTemperatureUnit();
        setupWindSwitch();
        setupPressureSwitch();
        setupFeelsLikeSwitch();
        setupHeadToolBar();

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
        citySpinner = findViewById(R.id.citySelection);
        showFeelsLike = findViewById(R.id.enableFeelsLike);
        showWindSwitch = findViewById(R.id.enableWindView);
        showPressure = findViewById(R.id.enablePressure);
        temperatureUnit = findViewById(R.id.temperatureUnit);
        headToolBar = findViewById(R.id.settings_toolbar);
    }

    private void setupHeadToolBar() {
        setSupportActionBar(headToolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
    }

    /**
     * Add toolbar some menus
     * @param menu - customizing menus of activity
     * @return true if menu displayed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    /**
     * Handle toolbar menu options
     * @param item selected item
     * @return true if processed here
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.actionCancel:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Intent appliedSettings = new Intent();
        appliedSettings.putExtra(MainActivity.mainActivityViewOptionsKey, settings);
        setResult(RESULT_OK, appliedSettings);
        finish();
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


