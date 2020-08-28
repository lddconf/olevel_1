package com.example.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.weather.diplayoption.WeatherDisplayOptions;
import com.example.weather.weather.CityWeatherSettings;
import com.example.weather.weather.WeatherDisplayActivity;
import com.example.weather.weather.WeatherDisplayFragment;
import com.example.weather.weatherprovider.openweatherorg.OpenWeatherOrgProvider;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Main Weather Info Activity
 */
public class MainActivity extends AppCompatActivity {
    private Toolbar mainToolBar;
    private WeatherDisplayOptions options;
    private ArrayList<CityWeatherSettings> mCityWeatherList;
    private int selectedIndex;
    private CitySelectionFragment citySelectionFragment;

    private NavigationView navigationView;
    private DrawerLayout drawer;

    private View layout;

    private static boolean verticalMode = true;

    private static final String mainActivitySelectedIndexKey = "AppMainActivitySelectedIndexKey";
    private static final String mainActivityCityListKey = "AppMainActivityCityListKey";
    public static final String mainActivityViewOptionsKey = "AppMainActivityViewOptions";
    private static final int settingsChangedRequestCode = 0x1;
    private static final boolean debug = false;

    private final static OpenWeatherOrgProvider weatherProvider = new OpenWeatherOrgProvider();

    private Handler handler;

    /**
     * Activity on create stuff
     * @param savedInstanceState - activity saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        restoreSettingsFromBundle(savedInstanceState);

        if ( options == null ) {
            options = new WeatherDisplayOptions();
        }

        setTheme(options.getThemeId());
        setContentView(R.layout.activity_nav);


        findViews();
/*
        verticalMode = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
*/

        if ( mCityWeatherList == null ) {
            //For fist run build data from old state
            prepareWeatherData();
            //Try to update data
            refreshWeatherData();
        }

        setupWeatherProvider();
        //setupCityViewFragment();
        setupActionBar();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mainToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        showCityList();

        //updateCityViewFragment();

         onDebug("onCreate");
    }

    private void setOnClickForSideMenuItems() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_city_list: {
                        showCityList();
                        drawer.closeDrawers();
                        break;
                    }
                    /*
                    case R.id.nav_gallery: {
                        setGalleryFragment();
                        drawer.close();
                        break;
                    }
                    case R.id.nav_slideshow: {
                        setSlideshowFragment();
                        drawer.close();
                        break;
                    }
                    */
                }
                return true;
            }
        });
    }

    private void setupCityViewFragment() {
        citySelectionFragment.displayTemperature(verticalMode);
        citySelectionFragment.enableSelection(!verticalMode);
        updateCityViewFragment();
    }

    private void showCityList() {
        if ( citySelectionFragment == null ) {
            citySelectionFragment = new CitySelectionFragment();
        }
        citySelectionFragment.setOnCityViewFragmentEvent(new CitySelectionFragment.OnCityViewFragmentEvent() {
            @Override
            public void onCityViewFragmentCreated() {
                setupCityViewFragment();
            }
        });
        setFragment(citySelectionFragment);
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("WS");
        ft.commit();
    }
/*
    private void setupCityViewFragment() {
        //Setup city selection frame view
        citySelectionFragment.displayTemperature(verticalMode);
        citySelectionFragment.enableSelection(!verticalMode);

        citySelectionFragment.setOnItemSelectedCallBack(index -> {

            //Close weather selection
            if (selectedIndex >= 0 && index < 0 ) {
                onBackPressed();
            }

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
        });
    }
*/

    private void setupWeatherProvider() {
        synchronized (weatherProvider) {
            weatherProvider.setErrorListener(new OpenWeatherOrgProvider.WeatherOnUpdateErrorListener() {
                @Override
                public void onErrorOccurredNotify(String error) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar snackbar = Snackbar.make(layout, error, Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                    });
                }
            });

            weatherProvider.setUpdateListener(new OpenWeatherOrgProvider.WeatherUpdatedListener() {
                @Override
                public void onWeatherUpdatedNotify(String city) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Wait for all data
                            if ( city == null ) {
                                prepareWeatherData();
                                updateCityViewFragment();
                            }
                        }
                    });
                }
            });

        }
    }

    private void updateCityViewFragment() {
        if ( citySelectionFragment != null ) {
            citySelectionFragment.setWeatherSettingsArray(mCityWeatherList);
            if (!verticalMode) {
                citySelectionFragment.setItemSelected(selectedIndex);
            }
        }
    }

    private void refreshWeatherData() {
        //Request new weather data from internet
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (weatherProvider) {
                    weatherProvider.updateWeatherList();
                }
            }
        }).start();
    }

    private void prepareWeatherData() {
        //Setup old data to display
        selectedIndex = -1;
        String[] cities = weatherProvider.getCitiesList();
        mCityWeatherList = new ArrayList<>(cities.length);
        for ( String city: cities ) {
            CityWeatherSettings cs = new CityWeatherSettings(city, weatherProvider.getWeatherFor(city), options);
            cs.addWeekForecastWeather(weatherProvider.getWeatherWeekForecastFor(city));
            mCityWeatherList.add( cs );
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
        switch (item.getItemId()) {
            case R.id.actionSettings:
                showSettings();
                return true;
            case R.id.actionAbout:
                showAbout();
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
        outState.putSerializable(mainActivitySelectedIndexKey, selectedIndex);
        outState.putSerializable(mainActivityViewOptionsKey, options);
        outState.putSerializable(mainActivityCityListKey, mCityWeatherList);

        onDebug("onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    private void restoreSettingsFromBundle(@Nullable Bundle savedInstanceState) {
        try {
            if ( savedInstanceState == null ) return;
            selectedIndex = savedInstanceState.getInt(mainActivitySelectedIndexKey);

            @SuppressWarnings("unchecked")
            ArrayList<CityWeatherSettings> restoredCityList = (ArrayList<CityWeatherSettings>)savedInstanceState.getSerializable(mainActivityCityListKey);
            if ( restoredCityList != null ) {
                mCityWeatherList = restoredCityList;
            } else {
                mCityWeatherList = new ArrayList<>();
            }

            WeatherDisplayOptions savedOptions = (WeatherDisplayOptions)savedInstanceState.getSerializable(mainActivityViewOptionsKey);
            if ( savedOptions != null ) {
                options = savedOptions;
                for ( CityWeatherSettings w: mCityWeatherList ) {
                    w.setWeatherDisplayOptions(options);
                }
                updateCityViewFragment();
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    /**
     * Activity in restore instance stuff
     * @param savedInstanceState - saved instance
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreSettingsFromBundle(savedInstanceState);

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

    private void showAbout() {
        Intent aboutActivity = new Intent(this, AboutActivity.class);
        aboutActivity.putExtra(mainActivityViewOptionsKey, options);
        startActivity(aboutActivity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( data == null ) {
            return;
        }
        //Apply new settings
        if ( requestCode == settingsChangedRequestCode ) {
            WeatherSettingsActivityCurrentStatus newViewSettings;
            newViewSettings = (WeatherSettingsActivityCurrentStatus) data.getSerializableExtra(mainActivityViewOptionsKey);
            assert newViewSettings != null;
            options = newViewSettings.getDisplayOptions();
            if ( resultCode == WeatherSettingsActivity.NEED_RELOAD_TO_APPLY_THEME ) {
                showSettings();
            }
            if ( resultCode == RESULT_OK ) {
                for (CityWeatherSettings w : mCityWeatherList) {
                    w.setWeatherDisplayOptions(options);
                }
                updateCityViewFragment();
                recreate();
            }
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

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        /*
        citySelectionFragment = (CitySelectionFragment)(getSupportFragmentManager().findFragmentById(R.id.city_list_fragment));
        */
        mainToolBar = findViewById(R.id.mainToolbar);
        layout = drawer;

    }
}