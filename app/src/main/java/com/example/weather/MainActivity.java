package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

        Log.d(mainActivityTAG, "onCreate");
        Toast.makeText(getApplicationContext(), "onCreate", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        Log.d(mainActivityTAG, "onDestroy");
        Toast.makeText(getApplicationContext(), "onDestroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(mainActivityTAG, "onStart");
        Toast.makeText(getApplicationContext(), "onStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(mainActivityTAG, "onStop");
        Toast.makeText(getApplicationContext(), "onStop", Toast.LENGTH_SHORT).show();
    }

    /**
     * Activity save instance state
     * @param outState - saved instance state
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(mainActivitySettingsKey, settings);
        Log.d(mainActivityTAG, "onSaveInstanceState");
        Toast.makeText(getApplicationContext(), "onSaveInstanceState", Toast.LENGTH_SHORT).show();

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
        Log.d(mainActivityTAG, "onRestoreInstanceState");
        Toast.makeText(getApplicationContext(), "onRestoreInstanceState", Toast.LENGTH_SHORT).show();
    }

    /**
     * Activity on pause stuff
     */
    @Override
    protected void onPause() {
        super.onPause();
        disableDateTimeUpdate();
        Log.d(mainActivityTAG, "onPause");
        Toast.makeText(getApplicationContext(), "onPause", Toast.LENGTH_SHORT).show();
    }

    /**
     * Activity on resume stuff
     */
    @Override
    protected void onResume() {
        super.onResume();
        initDateTimeUpdate();
        Log.d(mainActivityTAG, "onResume");
        Toast.makeText(getApplicationContext(), "onResume", Toast.LENGTH_SHORT).show();
    }

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
        SimpleDateFormat df = new SimpleDateFormat("E, dd MMM HH:mm", Locale.getDefault());
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

/**
 * Simple class for main activity settings
 */
class MainActivitySettings implements Serializable {
    private int temperature;
    private int feelsLike;
    private String cloudiness;
    private String city;
    private String tempUnit;

    /**
     * Default constructor for serializable/deserializable objects
     */
    public MainActivitySettings() {
        temperature = (int)(Math.random() * 40);
        feelsLike = (int)(Math.random() * 40);
        city = "Moscow";
        cloudiness = "cloudy";
        tempUnit = "\u2103";
    }

    /**
     * Get current temperature
     * @return current temperature
     */
    public int getTemperature() {
        return temperature;
    }

    /**
     * Get current feels like temperature
     * @return feels like temperature
     */
    public int getFeelsLike() {
        return feelsLike;
    }

    /**
     * Get current cloudiness status
     * @return cloudiness status string
     */
    public String getCloudiness() {
        return cloudiness;
    }

    /**
     * Get current city
     * @return current city
     */
    public String getCity() {
        return city;
    }

    /**
     * Get current temperature display unit
     * @return current temperature unit
     */
    public String getTempUnit() {
        return tempUnit;
    }
}