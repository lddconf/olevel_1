package com.example.weather;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.example.weather.diplayoption.WeatherDisplayOptionsFragment;
import com.example.weather.weather.SimpleWeatherProvider;
import com.example.weather.weather.WeatherProviderInterface;


import java.util.Objects;


/**
 * Weather ac
 */
public class WeatherSettingsActivity extends AppCompatActivity {
    private Spinner citySpinner;
    private WeatherSettingsActivityCurrentStatus settings;
    private Toolbar headToolBar;
    private final String weatherSettingsActivityKey = "WeatherSettingsActivityKey";
    private static WeatherProviderInterface weatherProvider = new SimpleWeatherProvider();
    private WeatherDisplayOptionsFragment weatherDisplayOptionsFragment;

    private static final boolean debug = false;

    public static final int NEED_RELOAD_TO_APPLY_THEME = RESULT_FIRST_USER + 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = (WeatherSettingsActivityCurrentStatus)getIntent().getSerializableExtra(MainActivity.mainActivityViewOptionsKey);

        setTheme(settings.getDisplayOptions().getThemeId());
        setContentView(R.layout.settings_activity);
        findViews();
        setupCityListSpinner();
        setupHeadToolBar();
        setupDisplayOptionsFrame();
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
        onDebug("onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onDebug("savedInstanceState");
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
        headToolBar = findViewById(R.id.settings_toolbar);
    }

    private void setupHeadToolBar() {
        setSupportActionBar(headToolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
    }

    private void setupDisplayOptionsFrame() {
        weatherDisplayOptionsFragment = new WeatherDisplayOptionsFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(WeatherDisplayOptionsFragment.DisplayOptionsKey, settings.getDisplayOptions());
        weatherDisplayOptionsFragment.setArguments(arguments);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace( R.id.fragment_container, weatherDisplayOptionsFragment);
        fragmentTransaction.commit();

        weatherDisplayOptionsFragment.setOnThemeChangedListener(new WeatherDisplayOptionsFragment.ThemeChanged() {
            @Override
            public void onThemeChanged() {
                //settings.setDisplayOptions(weatherDisplayOptionsFragment.getCurrentOptions());
                applyTheme();
            }
        });
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

    private void applyTheme() {
        Intent appliedSettings = new Intent();
        settings.setDisplayOptions(weatherDisplayOptionsFragment.getCurrentOptions());
        appliedSettings.putExtra(MainActivity.mainActivityViewOptionsKey, settings);
        setResult(NEED_RELOAD_TO_APPLY_THEME, appliedSettings);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent appliedSettings = new Intent();
        settings.setDisplayOptions(weatherDisplayOptionsFragment.getCurrentOptions());
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


