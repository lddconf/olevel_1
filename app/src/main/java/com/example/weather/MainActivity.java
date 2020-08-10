package com.example.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.diplayoption.WeatherDisplayOptions;
import com.example.weather.weather.CityWeatherSettings;
import com.example.weather.weather.SimpleWeatherProvider;
import com.example.weather.weather.WeatherDisplayActivity;
import com.example.weather.weather.WeatherDisplayFragment;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Main Weather Info Activity
 */
public class MainActivity extends AppCompatActivity {

    private TextView cityView;

    private Toolbar mainToolBar;
    private WeatherDisplayOptions options;
    private ArrayList<CityWeatherSettings> mCityWeatherList;
    private int selectedIndex;
    private CitySelectionFragment citySelectionFragment;
    private boolean verticalMode;

    private static final String mainActivitySelectedIndexKey = "AppMainActivitySelectedIndexKey";
    public static final String mainActivityViewOptionsKey = "AppMainActivityViewOptions";
    private static final int settingsChangedRequestCode = 0x1;
    private static final boolean debug = false;
    private static SimpleWeatherProvider weatherProvider = new SimpleWeatherProvider();
    /**
     * Activity on create stuff
     * @param savedInstanceState - activity saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        options = new WeatherDisplayOptions();
        findViews();

        verticalMode = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        prepareWeatherData();
        setupCityViewFragment();
        updateCityViewFragment();
        setupActionBar();
        onDebug("onCreate");
    }

    private void setupCityViewFragment() {
        //Setup city selection frame view
        citySelectionFragment.displayTemperature(verticalMode);
        citySelectionFragment.enableSelection(!verticalMode);

        citySelectionFragment.setOnItemSelectedCallBack(new ItemSelectedCallBack() {
            @Override
            public void itemSelected(int index) {
                selectedIndex = index;

                if ( selectedIndex < 0 | selectedIndex > mCityWeatherList.size() ) return;

                //Display new activity
                if ( verticalMode ) {
                    Intent newIntent = new Intent(getApplicationContext(), WeatherDisplayActivity.class);
                    newIntent.putExtra(WeatherDisplayFragment.WeatherDisplayOptionsKey, mCityWeatherList.get(selectedIndex));
                    startActivity(newIntent);
                } else {
                    WeatherDisplayFragment fragment = new WeatherDisplayFragment();
                    Bundle newBundle = new Bundle();
                    newBundle.putSerializable(WeatherDisplayFragment.WeatherDisplayOptionsKey, mCityWeatherList.get(selectedIndex));
                    fragment.setArguments(newBundle);

                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.weatherViewFragment, fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);


                    ft.addToBackStack("WS");
                    ft.commit();
                }
            }
        });
    }

    private void updateCityViewFragment() {
        citySelectionFragment.setWeatherSettingsArray(mCityWeatherList);
        if (!verticalMode) {
            citySelectionFragment.setItemSelected(selectedIndex);
        }
    }

    private void prepareWeatherData() {
        selectedIndex = -1;
        String[] cities = weatherProvider.getCitiesList();
        mCityWeatherList = new ArrayList<>(cities.length);
        for ( String city: cities ) {
            mCityWeatherList.add(new CityWeatherSettings(city, weatherProvider.getWeatherFor(city), options));
        }
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
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int countOfFragmentInManager = getSupportFragmentManager().getBackStackEntryCount();
        if(countOfFragmentInManager > 0) {
            getSupportFragmentManager().popBackStack("WS", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        selectedIndex = -1;
        citySelectionFragment.setItemSelected(selectedIndex);
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
        if (item.getItemId() == R.id.actionSettings) {
            showSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Activity save instance state
     * @param outState - saved instance state
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(mainActivitySelectedIndexKey, selectedIndex);
        outState.putSerializable(mainActivityViewOptionsKey, options);
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
            selectedIndex = savedInstanceState.getInt(mainActivitySelectedIndexKey);
            WeatherDisplayOptions savedOptions = (WeatherDisplayOptions)savedInstanceState.getSerializable(mainActivityViewOptionsKey);
            if ( savedOptions != null ) {
                options = savedOptions;
                updateCityViewFragment();
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
        onDebug("onPause");
    }

    /**
     * Activity on resume stuff
     */
    @Override
    protected void onResume() {
        super.onResume();
        onDebug("onResume");
    }


    /**
     * Dislay settings activity
     */
    private void showSettings() {
        Intent settingsActivity = new Intent(getApplicationContext(), WeatherSettingsActivity.class);
        settingsActivity.putExtra(mainActivityViewOptionsKey, new WeatherSettingsActivityCurrentStatus(citySelectionFragment.getSelectedCity(), options));
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
            options = newViewSettings.getDisplayOptions();

            for ( CityWeatherSettings w: mCityWeatherList ) {
                w.setWeatherDisplayOptions(options);
            }
            updateCityViewFragment();
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
        citySelectionFragment = (CitySelectionFragment)(getSupportFragmentManager().findFragmentById(R.id.city_list_fragmet));
        mainToolBar = findViewById(R.id.mainToolbar);
    }

}

