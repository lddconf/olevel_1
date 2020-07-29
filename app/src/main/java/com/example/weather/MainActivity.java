package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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


    private BroadcastReceiver dateTimeChangedReceiver;

    /**
     * Activity on create stuff
     * @param savedInstanceState - activity saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        updateLocation();
        updateTemp();
        updateFeelsLikeTempView();
        updateCloudinessView();
        updateWeatherView();
        setupSettingsView();

    }

    /**
     * Activity on pause stuff
     */
    @Override
    protected void onPause() {
        super.onPause();
        disableDateTimeUpdate();
    }

    /**
     * Activity on resume stuff
     */
    @Override
    protected void onResume() {
        super.onResume();
        initDateTimeUpdate();
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
        cityView.setText("Moscow");
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
        int temp = 13; //градусов цельсия
        String currentTemperature = Integer.toString(temp) + getString(R.string.temp_unit_celsius);
        temperatureView.setText(currentTemperature);
    }

    /**
     * Update weather feelslike status
     */
    private void updateFeelsLikeTempView() {
        int feelsLikeTemp = 11;
        feelsLikeView.setText(getString(R.string.feels_like) + " " + Integer.toString(feelsLikeTemp) + getString(R.string.temp_unit_celsius));
    }

    /**
     * Update weather cloudiness status
     */
    private void updateCloudinessView() {
        cloudinessView.setText(getString(R.string.cloudly));
    }

    /**
     * Update Weather status image
     */
    private void updateWeatherView() {
        weatherView.setImageResource(R.mipmap.ic_cloudly);
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