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

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.weather.diplayoption.WeatherDisplayOptionsFragment;
import com.example.weather.weather.CityWeatherSettings;
import com.example.weather.weather.WeatherDisplayFragment;
import com.example.weather.weatherprovider.openweatherorg.OpenWeatherOrgProvider;
import com.example.weather.weatherprovider.openweatherorg.OpenWeatherProviderEvent;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Main Weather Info Activity
 */
public class MainActivity extends AppCompatActivity {
    private Toolbar mainToolBar;
    //private WeatherDisplayOptions options;
    private ArrayList<CityWeatherSettings> mCityWeatherList;
    private CityWeatherSettings currentCityWeather;

    private CitySelectionFragment citySelectionFragment;

    private MenuItem citySelectItem;

    private NavigationView navigationView;
    private DrawerLayout drawer;

    private int lastSelectedSection;
    private boolean silentCityIndexSet = false;

    private View layout;

    private static final String mainActivityCityListKey = "AppMainActivityCityListKey";
    private static final String mainActivityLastSelectedSectionKey = "AppMainActivityLastSelectedSectionKey";
    private static final String mainActivityCurrentCityKey = "AppMainActivityCurrentCityKey";

    public static final String mainActivityViewOptionsKey = "AppMainActivityViewOptions";

    private static final int settingsChangedRequestCode = 0x1;
    private static final boolean debug = false;

    private final static OpenWeatherOrgProvider weatherProvider = new OpenWeatherOrgProvider();
    private UserSettings userSettings;

    private Handler handler;

    /**
     * Activity on create stuff
     * @param savedInstanceState - activity saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        userSettings = UserSettings.getUserSettings();

        restoreSettingsFromBundle(savedInstanceState);

        setTheme(userSettings.getOptions().getThemeId());
        setContentView(R.layout.activity_nav);

        findViews();

        if ( mCityWeatherList == null ) {
            //For fist run build data from old state
            prepareWeatherData();
        }

        //refreshWeatherDataFor(userSettings.getCurrentPlace());


        setupActionBar();
        setupNavigationDrawer();
        setOnClickForSideMenuItems();

        lastSelectedSection = -1;

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
                        if ( index >= 0 ) {
                            CityWeatherSettings wsettings = mCityWeatherList.get(index);
                            mCityWeatherList.remove(index);
                            mCityWeatherList.add(0, currentCityWeather);
                            currentCityWeather = wsettings;
                            navigateTo(R.id.nav_weather_details);
                        }
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
        String city = currentCityWeather.getCity();
        citySelectItem.setTitle(city);
    }

    private void setCurrentWeather() {
        navViewDisplayCity();
        WeatherDisplayFragment fragment = new WeatherDisplayFragment();
        Bundle newBundle = new Bundle();
        newBundle.putSerializable(WeatherDisplayFragment.WeatherDisplayOptionsKey, currentCityWeather);
        fragment.setArguments(newBundle);
        setFragment(fragment);

        //Request new weather if needed
        if ( currentCityWeather.getWeather() == null ) {
            refreshWeatherDataFor(currentCityWeather.getCity());
        }
    }

    private void setSettingsFragment() {
        WeatherDisplayOptionsFragment settings = new WeatherDisplayOptionsFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(WeatherDisplayOptionsFragment.DisplayOptionsKey, userSettings.getOptions());
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
            userSettings.setOptions(((WeatherDisplayOptionsFragment)lastFragment).getCurrentOptions());
            for (CityWeatherSettings w : mCityWeatherList) {
                w.setWeatherDisplayOptions(userSettings.getOptions());
            }
            currentCityWeather.setWeatherDisplayOptions(userSettings.getOptions());
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOWeatherEvent(OpenWeatherProviderEvent event) {
        switch (event.getResultCode()) {
            case CONNECTION_ERROR:
                displayMessage(event.getErrorDescription());
                break;
            case UPDATE_SUCCESSFUL:
                updateWeatherData();
                updateWeatherView();
                break;
            case CITY_NOT_FOUND_ERROR:
                displayMessage("City not found");
                break;
        }
    }
/*
    private void setupWeatherProvider() {
        synchronized (weatherProvider) {
            weatherProvider.getBus().register(this);

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

                        }
                    });
                }
            });

        }
    }
*/
    private void updateWeatherView() {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        if (fragments.size() == 0) return;
        Fragment lastFragment = fragments.get(fragments.size() - 1);
        if ( lastFragment == null) return;
        if ( lastFragment instanceof WeatherDisplayFragment ) {
            ((WeatherDisplayFragment)lastFragment).setWeather(currentCityWeather.getWeather());
        }
        if ( lastFragment instanceof CitySelectionFragment ) {
            ((CitySelectionFragment)lastFragment).setWeatherSettingsArray(mCityWeatherList);
        }
    }

    private void refreshCachedWeatherData() {
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

    private void refreshWeatherDataFor(String city) {
        //Request new weather data from internet
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (weatherProvider) {
                    weatherProvider.updateWeatherFor(city);
                }
            }
        }).start();
    }

    private void refreshWeatherDataForOtherPlaces() {
        //Request new weather data from internet
        new Thread(new Runnable() {
            @Override
            public void run() {
                for ( CityWeatherSettings cs: mCityWeatherList ) {
                    synchronized (weatherProvider) {
                        weatherProvider.updateWeatherFor(cs.getCity());
                    }
                }
            }
        }).start();
    }

    /**
     * Setup class weather info
     */
    private void prepareWeatherData() {
        currentCityWeather = new CityWeatherSettings(userSettings.getCurrentPlace(),
                    weatherProvider.getWeatherFor(userSettings.getCurrentPlace()),
                    userSettings.getOptions());

        String[] otherPlaces = userSettings.getOtherPacesList();
        mCityWeatherList = new ArrayList<>(otherPlaces.length);

        for ( String city: otherPlaces ) {
            CityWeatherSettings cs = new CityWeatherSettings(city, weatherProvider.getWeatherFor(city), userSettings.getOptions());
            cs.addWeekForecastWeather(weatherProvider.getWeatherWeekForecastFor(city));
            mCityWeatherList.add(cs);
        }
    }

    private void updateWeatherData() {
        currentCityWeather.setWeather(weatherProvider.getWeatherFor(currentCityWeather.getCity()));
        for ( CityWeatherSettings cs: mCityWeatherList ) {
            cs.setWeather( weatherProvider.getWeatherFor(cs.getCity()));
            cs.addWeekForecastWeather(weatherProvider.getWeatherWeekForecastFor(cs.getCity()));
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
                navigateTo(R.id.nav_weather_details);
                return;
            }
        }
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
            case R.id.actionRefresh:
                if (lastSelectedSection == R.id.nav_favorites ) {
                    refreshWeatherDataForOtherPlaces();

                } else {
                    refreshWeatherDataFor(currentCityWeather.getCity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onStorePlacesInUserSettings() {
        //Store all current places in user settings
        userSettings.setCurrentPlace(currentCityWeather.getCity());

        String[] otherPlaces = new String[mCityWeatherList.size()];

        int i = 0;
        for ( CityWeatherSettings w: mCityWeatherList ) {
            otherPlaces[i++] = w.getCity();
        }
        userSettings.removeAllOtherPlaces();
        userSettings.addOtherPlaces(otherPlaces);
    }

    /**
     * Activity save instance state
     * @param outState - saved instance state
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(mainActivityCurrentCityKey, currentCityWeather);
        outState.putSerializable(mainActivityCityListKey, mCityWeatherList);
        outState.putSerializable(mainActivityLastSelectedSectionKey, lastSelectedSection);
        onStorePlacesInUserSettings();

        onDebug("onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    private void restoreSettingsFromBundle(@Nullable Bundle savedInstanceState) {
        try {
            if ( savedInstanceState == null ) return;
            lastSelectedSection = savedInstanceState.getInt(mainActivityLastSelectedSectionKey);
            currentCityWeather = (CityWeatherSettings)savedInstanceState.getSerializable(mainActivityCurrentCityKey);
            @SuppressWarnings("unchecked")
            ArrayList<CityWeatherSettings> restoredCityList = (ArrayList<CityWeatherSettings>)savedInstanceState.getSerializable(mainActivityCityListKey);
            if ( restoredCityList != null ) {
                mCityWeatherList = restoredCityList;
            } else {
                mCityWeatherList = new ArrayList<>();
            }

            for ( CityWeatherSettings w: mCityWeatherList ) {
                w.setWeatherDisplayOptions(userSettings.getOptions());
            }
            currentCityWeather.setWeatherDisplayOptions(userSettings.getOptions());
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
        updateWeatherView();
        onDebug("onRestoreInstanceState");
    }

    /**
     * Activity on pause stuff
     */
    @Override
    protected void onPause() {
        super.onPause();
        weatherProvider.getBus().unregister(this);
        onDebug("onPause");
    }

    /**
     * Activity on resume stuff
     */
    @Override
    protected void onResume() {
        super.onResume();
        //To receive events on WeatherProvider
        weatherProvider.getBus().register(this);

        //Startup view
        navViewDisplayCity();
        if (getSupportFragmentManager().getBackStackEntryCount() == 0 ) {
            navigateTo(R.id.nav_weather_details);
        }

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