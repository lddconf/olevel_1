package com.example.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Main Weather Info Activity
 */
public class MainActivity extends AppCompatActivity {

    private TextView cityView;
    private TextView dateTimeView;
    private TextView temperatureView;
    private TextView feelsLikeView;
    private TextView cloudinessView;
    private TextView windView;
    private TextView pressureView;
    private ImageView weatherView;
    private Toolbar mainToolBar;
    private MainActivitySettings settings;

    private static final String mainActivitySettingsKey = "AppMainActivitySettings";
    static final String mainActivityViewOptionsKey = "AppMainActivityViewOptions";
    private static final int settingsChangedRequestCode = 0x1;
    private static final boolean debug = false;

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
        setupCityView();
        setupDateTimeViewOnClick();
        setupTemperatureViewOnClick();
        setupActionBar();
        updateViews();
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
     * Setup action bar
     */
    void setupActionBar() {
        setSupportActionBar(mainToolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        mainToolBar.setBackground(new ColorDrawable(getResources().getColor(R.color.background)));
    }


    /**
     * Add toolbar some menus
     * @param menu - customizing menus of activity
     * @return true if menu displayed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Process toolbar menus onclick action
     * @param item item to processing
     * @return true if item was processed
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionSettings:
                showSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
     * Update all changeable views
     */
    private void updateViews() {
        updateLocation();
        updateTemp();
        updateFeelsLikeTempView();
        updateCloudinessView();
        updateWeatherView();
        updateWindView();
        updatePressureView();
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
                updateViews();
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
     * Dislay settings activity
     */
    private void showSettings() {
        Intent settingsActivity = new Intent(getApplicationContext(), WeatherSettingsActivity.class);
        settingsActivity.putExtra(mainActivityViewOptionsKey, new WeatherSettingsActivityCurrentStatus(settings));
        startActivityForResult(settingsActivity, settingsChangedRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( data == null ) {
            return;
        }
        //Apply new settings
        if ( requestCode == settingsChangedRequestCode && resultCode == RESULT_OK ) {
            WeatherSettingsActivityCurrentStatus newViewSettings;
            newViewSettings = (WeatherSettingsActivityCurrentStatus)data.getSerializableExtra(mainActivityViewOptionsKey);
            assert newViewSettings != null;
            settings.setCity(newViewSettings.getCity());
            settings.setUseCelsiusUnit(!newViewSettings.isFahrenheitTempUnit());
            settings.setShowFeelsLike(newViewSettings.isShowFeelsLike());
            settings.setShowWind(newViewSettings.isShowWindSpeed());
            settings.setShowPressure(newViewSettings.isShowPressure());
            updateViews();
        }
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
        String currentTemperature = Integer.toString(settings.getTemperature());
        if ( settings.useCelsiusUnit() ) {
            currentTemperature += getString(R.string.temp_unit_celsius);
        } else {
            currentTemperature += getString(R.string.temp_unit_fahrenheit);
        }
        temperatureView.setText(currentTemperature);
    }

    /**
     * Update weather feels like status
     */
    private void updateFeelsLikeTempView() {
        if ( settings.isShowFeelsLike() ) {
            feelsLikeView.setVisibility(View.VISIBLE);
            String tempUnit = getString(R.string.temp_unit_celsius);
            if ( !settings.useCelsiusUnit() ) {
                tempUnit = getString(R.string.temp_unit_fahrenheit);
            }
            feelsLikeView.setText(String.format("%s %s%s", getString(R.string.feels_like), settings.getFeelsLikeTemp(), tempUnit));
        } else {
            feelsLikeView.setVisibility(View.GONE);
        }
    }

    private void updateWindView() {
        if (settings.isShowWind()) {
            windView.setVisibility(View.VISIBLE);
            windView.setText(String.format(Locale.getDefault(), "%.1f m/s", settings.getWindSpeed()));
        } else {
            windView.setVisibility(View.GONE);
        }
    }

    private void updatePressureView() {
        if (settings.isShowPressure()) {
            pressureView.setVisibility(View.VISIBLE);
            pressureView.setText(String.format(Locale.getDefault(),"%d mm", settings.getPressureBar()));
        } else {
            pressureView.setVisibility(View.GONE);
        }
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
     * @param textToPrint - some debug text
     */
    private void onDebug(String textToPrint) {
        if ( debug ) {
            String mainActivityTAG = "MainActivity";
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
        windView = findViewById(R.id.windView);
        pressureView = findViewById(R.id.pressureView);
        mainToolBar = findViewById(R.id.mainToolbar);
    }

}

