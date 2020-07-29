package com.example.weather;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import java.lang.reflect.Array;
import java.util.Arrays;

public class WeatherSettingsActivity extends AppCompatActivity {
    private Button    okButton;
    private Button    cancelButton;
    private Spinner   citySpinner;
    private Switch    showWindSwitch;
    private Switch    showPressure;
    private Switch    showFeelsLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        findViews();
        setupOkButtonOnClickListener();
        setupCancelButtonOnClickListener();
        setupCityListSpinner();
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

    /**
     * Simple method for supported cities
     * @return
     */
    private String[] getCitiesList() {
        String[] cities = new String[] {"Moscow", "New York", "Berlin", "Paris"};
        Arrays.sort(cities);
        return cities;
    }

    private void setupCityListSpinner() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getCitiesList());
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
    }

}