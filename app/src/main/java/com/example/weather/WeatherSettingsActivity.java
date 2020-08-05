package com.example.weather;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.io.Serializable;


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

    private final String weatherSettingsActivityTAG = "WeatherSettingsActivity";
    private final String weatherSettingsActivityKey = "WeatherSettingsActivityKey";
    private static WeatherProviderInterface weatherProvider = new SimpleWeatherProvider();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        settings = new WeatherSettingsActivityCurrentStatus();

        findViews();
        setupOkButtonOnClickListener();
        setupCancelButtonOnClickListener();
        setupCityListSpinner();
        setupTemperatureUnit();
        updateShowWindSpeed();
        updateShowPressure();
        updateShowFeelsLike();

        Log.d(weatherSettingsActivityTAG, "onCreate");
        Toast.makeText(getApplicationContext(), "onCreate", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(weatherSettingsActivityTAG, "onDestroy");
        Toast.makeText(getApplicationContext(), "onDestroy", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(weatherSettingsActivityTAG, "onStart");
        Toast.makeText(getApplicationContext(), "onStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(weatherSettingsActivityTAG, "onStop");
        Toast.makeText(getApplicationContext(), "onStop", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(weatherSettingsActivityTAG, "onPause");
        Toast.makeText(getApplicationContext(), "onPause", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(weatherSettingsActivityTAG, "onResume");
        Toast.makeText(getApplicationContext(), "onResume", Toast.LENGTH_SHORT).show();
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
        Log.d(weatherSettingsActivityTAG, "onSaveInstanceState");
        Toast.makeText(getApplicationContext(), "onSaveInstanceState", Toast.LENGTH_SHORT).show();
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
        Log.d(weatherSettingsActivityTAG, "savedInstanceState");
        Toast.makeText(getApplicationContext(), "savedInstanceState", Toast.LENGTH_SHORT).show();
    }

    private void setupTemperatureUnit() {
        temperatureUnit.setChecked(settings.isFahrenheitTempUnit());
        updateTemperatureUnit();
        temperatureUnit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateTemperatureUnit();
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
        citySpinner.setSelection(arrayAdapter.getPosition(settings.getCity()));
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

class WeatherSettingsActivityCurrentStatus implements Serializable {
    private String city;
    private boolean useFahrenheitTempUnit;
    private boolean showWindSpeed;
    private boolean showPressure;
    private boolean showFeelsLike;

    public WeatherSettingsActivityCurrentStatus() {
        this("Paris", false, false, false, false);
    }

    public WeatherSettingsActivityCurrentStatus(String city, boolean useFahrenheitTempUnit, boolean showWindSpeed, boolean showPressure, boolean showFeelsLike) {
        this.city = city;
        this.useFahrenheitTempUnit = useFahrenheitTempUnit;
        this.showWindSpeed = showWindSpeed;
        this.showPressure = showPressure;
        this.showFeelsLike = showFeelsLike;
    }

    public String getCity() {
        return city;
    }

    public boolean isFahrenheitTempUnit() {
        return useFahrenheitTempUnit;
    }

    public boolean isShowWindSpeed() {
        return showWindSpeed;
    }

    public boolean isShowPressure() {
        return showPressure;
    }

    public boolean isShowFeelsLike() {
        return showFeelsLike;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setTemperatureUnit(boolean useFahrenheitTempUnit) {
        this.useFahrenheitTempUnit = useFahrenheitTempUnit;
    }

    public void setShowWindSpeed(boolean showWindSpeed) {
        this.showWindSpeed = showWindSpeed;
    }

    public void setShowPressure(boolean showPressure) {
        this.showPressure = showPressure;
    }

    public void setShowFeelsLike(boolean showFeelsLike) {
        this.showFeelsLike = showFeelsLike;
    }
}
