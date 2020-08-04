package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Main Weather Info Activity
 */
public class MainActivity extends AppCompatActivity {

    private TextView cityView;
    private TextView dateTimeView;
    private TextView temperatureView;
    private TextView feelsLikeView;
    private TextView cloudinessView;
    private ImageView weatherView;
    private ImageView settingsViewButton;

    private MainActivitySettings settings;

    private final String mainActivitySettingsKey = "AppMainActivitySettings";
    private final String mainActivityTAG = "MainActivity";
    private BroadcastReceiver dateTimeChangedReceiver;

    private static final boolean debug = false;

    /**
     * Activity on create stuff
     * @param savedInstanceState - activity saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = new MainActivitySettings();

        findViews();
        updateLocation();
        updateTemp();
        updateFeelsLikeTempView();
        updateCloudinessView();
        updateWeatherView();
        setupSettingsView();
        setupCityView();
        setupDateTimeViewOnClick();
        setupTemperatureViewOnClick();

        onDebug("onCreate");
    }

    @Override
    protected void onDestroy() {
        onDebug("onDestroy");
        super.onDestroy();
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

    /**
     * Activity save instance state
     * @param outState - saved instance state
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(mainActivitySettingsKey, settings);
        onDebug("onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    /**
     * Activity in restore instance stuff
     * @param savedInstanceState - saved instance
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            MainActivitySettings savedSettings = (MainActivitySettings)savedInstanceState.getSerializable(mainActivitySettingsKey);
            if ( savedSettings != null ) {
                settings = savedSettings;
                updateLocation();
                updateTemp();
                updateFeelsLikeTempView();
                updateCloudinessView();
                updateWeatherView();
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        onDebug("onRestoreInstanceState");
    }

    /**
     * Activity on pause stuff
     */
    @Override
    protected void onPause() {
        super.onPause();
        disableDateTimeUpdate();

        onDebug("onPause");
    }

    /**
     * Activity on resume stuff
     */
    @Override
    protected void onResume() {
        super.onResume();
        initDateTimeUpdate();
        onDebug("onResume");
    }

    /**
     * Setup settings view button
     */
    private void setupSettingsView() {
        settingsViewButton.setImageResource(R.mipmap.ic_settings);
        settingsViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsActivity = new Intent(getApplicationContext(), WeatherSettingsActivity.class);
                startActivity(settingsActivity);
            }
        });
    }

    /**
     * Setup On this day history
     */
    private void setupDateTimeViewOnClick() {
        dateTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date currentDate = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("MMMM/d", Locale.getDefault());
                String url = getString(R.string.on_this_day) + df.format(currentDate).toLowerCase();
                Uri uri = Uri.parse(url);
                Intent onThisDayBrowser = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(onThisDayBrowser);
            }
        });
    }

    private void setupTemperatureViewOnClick() {
        temperatureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.format(getString(R.string.weather_yandex), settings.getCity());
                Uri uri = Uri.parse(url.toLowerCase());
                Intent weatherBrowser = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(weatherBrowser);
            }
        });
    }
    /**
     * Enable auto update date-time view
     */
    private void initDateTimeUpdate() {
        updateDate();
        dateTimeChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateDate();
            }
        };
        IntentFilter dateTimeUpdateFilter = new IntentFilter();
        dateTimeUpdateFilter.addAction(Intent.ACTION_TIME_CHANGED); //manually time set
        dateTimeUpdateFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        dateTimeUpdateFilter.addAction(Intent.ACTION_DATE_CHANGED); //manually date set
        dateTimeUpdateFilter.addAction(Intent.ACTION_TIME_TICK);    //every 1 minute
        registerReceiver(dateTimeChangedReceiver, dateTimeUpdateFilter);
    }

    /**
     * Disable auto update date-time view
     */
    private void disableDateTimeUpdate() {
       unregisterReceiver(dateTimeChangedReceiver);
    }

    private void setupCityView() {
        cityView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(getString(R.string.city_about) + settings.getCity());
                Intent cityDetailsBrowser = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(cityDetailsBrowser);
            }
        });
    }
    /**
     * Update location view
     */
    private void updateLocation() {
        cityView.setText(settings.getCity());
    }

    /**
     * Update date-time status view
     */
    private void updateDate() {
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());
        String dateTime = df.format(currentDate);
        dateTimeView.setText(dateTime);
    }

    /**
     * Update temperature value in view
     */
    private void updateTemp() {
        String currentTemperature = settings.getTemperature() + settings.getTempUnit();
        temperatureView.setText(currentTemperature);
    }

    /**
     * Update weather feels like status
     */
    private void updateFeelsLikeTempView() {
        feelsLikeView.setText(String.format("%s %s%s", getString(R.string.feels_like), settings.getFeelsLike(), settings.getTempUnit()));
    }

    /**
     * Update weather cloudiness status
     */
    private void updateCloudinessView() {
        cloudinessView.setText(settings.getCloudiness());
    }

    /**
     * Update Weather status image
     */
    private void updateWeatherView() {
        if ( settings.getCloudiness().equals(getString(R.string.cloudy))) {
            weatherView.setImageResource(R.mipmap.ic_cloudly);
        }
    }

    /**
     * Debug purposes
     * @param textToPrint
     */
    private void onDebug(String textToPrint) {
        if ( debug ) {
            Log.d(mainActivityTAG, textToPrint);
            Toast.makeText(getApplicationContext(), textToPrint, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Find activity views
     */
    private void findViews() {
        cityView = findViewById( R.id.cityView );
        dateTimeView = findViewById( R.id.dateView );
        temperatureView = findViewById( R.id.tempView );
        feelsLikeView = findViewById( R.id.feelsLike );
        weatherView = findViewById( R.id.imageView);
        cloudinessView = findViewById( R.id.cloudinessView);
        settingsViewButton = findViewById(R.id.settingsButton);
    }

}

