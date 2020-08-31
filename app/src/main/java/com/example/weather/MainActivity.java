package com.example.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.weather.diplayoption.WeatherDisplayOptions;
import com.example.weather.diplayoption.WeatherDisplayOptionsFragment;
import com.example.weather.weather.CityWeatherSettings;
import com.example.weather.weather.WeatherDisplayFragment;
import com.example.weather.weatherprovider.openweatherorg.OpenWeatherOrgProvider;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Main Weather Info Activity
 */
public class MainActivity extends AppCompatActivity {
    private Toolbar mainToolBar;
    private WeatherDisplayOptions options;
    private ArrayList<CityWeatherSettings> mCityWeatherList;
    private CitySelectionFragment citySelectionFragment;

    private MenuItem citySelectItem;

    private NavigationView navigationView;
    private DrawerLayout drawer;

    private int selectedIndex;
    private int lastSelectedSection;

    private View layout;

    private static final String mainActivitySelectedIndexKey = "AppMainActivitySelectedIndexKey";
    private static final String mainActivityCityListKey = "AppMainActivityCityListKey";
    private static final String mainActivityLastSelectedSectionKey = "AppMainActivityLastSelectedSectionKey";

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
            options.setTemperatureUnit(false);
        }

        setTheme(options.getThemeId());
        setContentView(R.layout.activity_nav);


        findViews();

        if ( mCityWeatherList == null ) {
            //For fist run build data from old state
            prepareWeatherData();
            //Try to update data
            refreshWeatherData();
        }

        setupWeatherProvider();
        setupActionBar();
        setupNavigationDrawer();
        setOnClickForSideMenuItems();

        selectedIndex = 0;
        navViewDisplayCity();

        lastSelectedSection = -1;
        if (getSupportFragmentManager().getBackStackEntryCount() == 0 ) {
            navigateTo(R.id.nav_weather_details);
        }
        onDebug("onCreate");
    }

    private void setupNavigationDrawer() {
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_favorites, R.id.nav_about, R.id.nav_weather_details)
                .setDrawerLayout(drawer)
                .build();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mainToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupCityViewFragment() {
        boolean verticalMode = true;
        citySelectionFragment.displayTemperature(verticalMode);
        citySelectionFragment.enableSelection(!verticalMode);
        updateWeatherView();
    }

    private void setCityListFragment() {
        citySelectionFragment = new CitySelectionFragment();
        citySelectionFragment.setOnCityViewFragmentEvent(new CitySelectionFragment.OnCityViewFragmentEvent() {
            @Override
            public void onCityViewFragmentCreated() {
                setupCityViewFragment();
                citySelectionFragment.setOnItemSelectedCallBack(index -> {
                    //Close weather selection
                    if ( index < mCityWeatherList.size() ) {
                        CityWeatherSettings wsettings = mCityWeatherList.get(index);
                        mCityWeatherList.remove(wsettings);
                        mCityWeatherList.add(0, wsettings);
                        selectedIndex = 0;
                        navigateTo(R.id.nav_weather_details);
                    }
                });
            }
        });
        setFragment(citySelectionFragment);
    }

    private void setAboutFragment() {
        setFragment(new AboutFragment());
    }

    private void setFeedBackFragment() {
        setFragment(new FeedBackFragment());
    }

    private void navViewDisplayCity() {
        if ( selectedIndex >=0 && mCityWeatherList.size() > selectedIndex ) {
            String city = mCityWeatherList.get(selectedIndex).getCurrentCity();
            citySelectItem.setTitle(city);
        }
    }

    private void setCurrentWeather() {
        navViewDisplayCity();

        WeatherDisplayFragment fragment = new WeatherDisplayFragment();
        Bundle newBundle = new Bundle();
        newBundle.putSerializable(WeatherDisplayFragment.WeatherDisplayOptionsKey, mCityWeatherList.get(selectedIndex));
        fragment.setArguments(newBundle);
        setFragment(fragment);
    }

    private void setSettingsFragment() {
        WeatherDisplayOptionsFragment settings = new WeatherDisplayOptionsFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(WeatherDisplayOptionsFragment.DisplayOptionsKey, options);
        settings.setArguments(arguments);
        settings.setOnThemeChangedListener(() -> {
            recreate();
        });
        setFragment(settings);
    }




    private void setFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("WS");
        ft.commit();
    }

    private void setOnClickForSideMenuItems() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return showNavigateFragment(item.getItemId());
            }
        });
    }

    private void navigateTo(int id) {
        navigationView.setCheckedItem(id);
        showNavigateFragment(id);

    }

    private void saveOptionIfNeeded() {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        if (fragments.size() == 0) return;
        Fragment lastFragment = fragments.get(fragments.size() - 1);
        if ( lastFragment instanceof WeatherDisplayOptionsFragment ) {
            options = ((WeatherDisplayOptionsFragment)lastFragment).getCurrentOptions();
            for (CityWeatherSettings w : mCityWeatherList) {
                w.setWeatherDisplayOptions(options);
            }
        }
    }

    private boolean showNavigateFragment(int id) {
        saveOptionIfNeeded();
        drawer.closeDrawers();
        if (lastSelectedSection != id ) {
            switch (id) {
                case R.id.nav_favorites:
                    setCityListFragment();
                    break;
                case R.id.nav_about:
                    setAboutFragment();
                    break;
                case R.id.nav_settings:
                    setSettingsFragment();
                    break;
                case R.id.nav_feedback:
                    setFeedBackFragment();
                    break;
                case R.id.nav_weather_details:
                    setCurrentWeather();
                    break;
            }
            lastSelectedSection = id;
        }
        return true;
    }

    private void displayMessage(String msg) {
        Snackbar snackbar = Snackbar.make(layout, msg, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void setupWeatherProvider() {
        synchronized (weatherProvider) {
            weatherProvider.setErrorListener(new OpenWeatherOrgProvider.WeatherOnUpdateErrorListener() {
                @Override
                public void onErrorOccurredNotify(String error) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            displayMessage(error);
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
                                updateWeatherView();
                            }
                        }
                    });
                }
            });

        }
    }

    private void updateWeatherView() {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        if (fragments.size() == 0) return;
        Fragment lastFragment = fragments.get(fragments.size() - 1);
        if ( lastFragment == null) return;
        if ( lastFragment instanceof WeatherDisplayFragment ) {
            ((WeatherDisplayFragment)lastFragment).setWeather(mCityWeatherList.get(selectedIndex).getWeather());
        }
        if ( lastFragment instanceof CitySelectionFragment ) {
            citySelectionFragment.setWeatherSettingsArray(mCityWeatherList);
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
        selectedIndex = 0;
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }


        int countOfFragmentInManager = getSupportFragmentManager().getBackStackEntryCount();
        if(countOfFragmentInManager > 0) {
            getSupportFragmentManager().popBackStack("WS", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            if (lastSelectedSection != R.id.nav_weather_details ) {
                selectedIndex = 0;
                navigateTo(R.id.nav_weather_details);
                return;
            }
        }
        //getSupportFragmentManager().popBackStack("WS", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        //citySelectionFragment.setItemSelected(selectedIndex);
        super.onBackPressed();
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
            case R.id.actionFindLocation:
            case R.id.actionAdd:
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
        outState.putSerializable(mainActivityLastSelectedSectionKey, lastSelectedSection);

        onDebug("onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    private void restoreSettingsFromBundle(@Nullable Bundle savedInstanceState) {
        try {
            if ( savedInstanceState == null ) return;
            selectedIndex = savedInstanceState.getInt(mainActivitySelectedIndexKey);
            lastSelectedSection = savedInstanceState.getInt(mainActivityLastSelectedSectionKey);
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
                updateWeatherView();
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
        navViewDisplayCity();
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
        //navigateTo(lastSelectedSection);
        onDebug("onResume");
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
        mainToolBar = findViewById(R.id.mainToolbar);

        Menu menuNav = navigationView.getMenu();
        citySelectItem = menuNav.findItem(R.id.nav_weather_details);

        layout = drawer;

    }
}