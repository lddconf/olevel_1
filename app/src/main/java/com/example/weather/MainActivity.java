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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.weather.diplayoption.WeatherDisplayOptionsFragment;
import com.example.weather.weather.CityWeatherSettings;
import com.example.weather.weather.WeatherDisplayFragment;
import com.example.weather.weather.WeatherEntity;
import com.example.weather.weatherprovider.OpenWeatherOrgService;
import com.example.weather.weatherprovider.openweatherorg.OpenWeatherProviderEvent;
import com.example.weather.weatherprovider.openweatherorg.OpenWeatherSearchResultEvent;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.Subscribe;

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
    private View layout;

    private static final String mainActivityCityListKey = "AppMainActivityCityListKey";
    private static final String mainActivityLastSelectedSectionKey = "AppMainActivityLastSelectedSectionKey";
    private static final String mainActivityCurrentCityKey = "AppMainActivityCurrentCityKey";

    public static final String mainActivityViewOptionsKey = "AppMainActivityViewOptions";

    private static final int settingsChangedRequestCode = 0x1;
    private static final boolean debug = false;

    private UserSettings userSettings;
    private CitySearchDialog searchDialog;

    private boolean isBinded = false;
    private OpenWeatherOrgService.OpenWeatherOrgBinder openWeatherOrgService;

    private ServiceConnection openWeatherOrgServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            openWeatherOrgService = (OpenWeatherOrgService.OpenWeatherOrgBinder)iBinder;
            isBinded = true;

            //Refresh data for current city
            openWeatherOrgService.refreshWeatherDataFor(currentCityWeather.getCity());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            openWeatherOrgService = null;
            isBinded = false;
        }
    };


    /**
     * Activity on create stuff
     * @param savedInstanceState - activity saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSettings = UserSettings.getUserSettings();

        restoreSettingsFromBundle(savedInstanceState);

        setTheme(userSettings.getOptions().getThemeId());
        setContentView(R.layout.activity_nav);

        findViews();

        bindOpenWeatherService();

        if ( mCityWeatherList == null ) {
            //For fist run build data from old state
            restoreSettings();
        }

        setupActionBar();
        setupNavigationDrawer();
        setOnClickForSideMenuItems();

        lastSelectedSection = -1;
        searchDialog =  new CitySearchDialog();
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
        String city = currentCityWeather.getCity().getName();
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
            if (isBinded) {
                Objects.requireNonNull(openWeatherOrgService).refreshWeatherDataFor(currentCityWeather.getCity());
            }
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


    // Weather update handler
    private BroadcastReceiver onWeatherUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final OpenWeatherProviderEvent result = (OpenWeatherProviderEvent)intent.getSerializableExtra(OpenWeatherOrgService.BROADCAST_ACTION_WEATHER_UPDATE_RESULT);

            if ( result ==  null ) return;
            // Потокобезопасный вывод данных
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onOWeatherEvent(result);
                }
            });
        }
    };

    private void onOWeatherEvent(@NonNull  OpenWeatherProviderEvent event) {
        switch (event.getResultCode()) {
            case CONNECTION_ERROR:
                displayMessage(event.getErrorDescription());
                break;
            case REQUEST_COMPLETED:
                updateWeatherData();
                updateWeatherView();
                break;
        }
    }

    private BroadcastReceiver onCitySearchResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final  OpenWeatherSearchResultEvent event
                    = (OpenWeatherSearchResultEvent)intent.getSerializableExtra(OpenWeatherOrgService.BROADCAST_ACTION_SEARCH_RESULT);
            if ( event != null ) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onFoundCities(event);
                    }
                });
            }
        }
    };

    //Handler for city search result
    public void onFoundCities(@NonNull OpenWeatherSearchResultEvent event) {
        switch (event.getResultCode()) {
            case CONNECTION_ERROR:
                displayMessage(event.getErrorDescription());
                break;
            case REQUEST_COMPLETED:
                //Nothing found
                CitySelectionDialogFragment dialogFragment =
                        CitySelectionDialogFragment.newInstance(event.getSearchDetails(), event.getKeyword());
                dialogFragment.show(getSupportFragmentManager(),
                        "dialog_fragment");
                break;
        }
    }

    @Subscribe
    public void addNewCity(SearchEngineCitySelectedEvent event) {
        if ( event.getCityID() != null ) {
            if ( currentCityWeather.getCity().equals( event.getCityID()) ) {
                navigateTo(R.id.nav_weather_details);
                return; //already exist in current place
            }

            //Check selected city for other places
            for ( CityWeatherSettings cws: mCityWeatherList
                 ) {
                if ( cws.getCity().equals(event.getCityID())) {
                    mCityWeatherList.add(0, currentCityWeather);
                    mCityWeatherList.remove(cws);
                    currentCityWeather = cws;
                    navViewDisplayCity();
                    navigateTo(R.id.nav_favorites);
                    navigateTo(R.id.nav_weather_details);
                    return;
                }
            }

            //Add new city
            if ( !currentCityWeather.getCity().equals( event.getCityID()) ) {
                mCityWeatherList.add(0, currentCityWeather);
                currentCityWeather = new CityWeatherSettings( event.getCityID(), new WeatherEntity(), userSettings.getOptions());
                if (isBinded) {
                    Objects.requireNonNull(openWeatherOrgService).refreshWeatherDataFor(currentCityWeather.getCity());
                }
                navViewDisplayCity();
            }
            navigateTo(R.id.nav_favorites);
            navigateTo(R.id.nav_weather_details);
        }
    }

    public void onCitySearchRequested(String key) {
        if (isBinded) {
            Objects.requireNonNull(openWeatherOrgService).findForecastFor(key);
        }
    }

    //Search city
    private void searchCity() {
        searchDialog.setCancelable(false);
        searchDialog.show(getSupportFragmentManager(), getString(R.string.search_dialog_title));
    }

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

    private void refreshWeatherDataForOtherPlaces() {
        //Request new weather data from internet
        if ( !isBinded) return;
        for (int i = 0; i < mCityWeatherList.size(); i++) {
            Objects.requireNonNull(openWeatherOrgService).refreshWeatherDataFor(mCityWeatherList.get(i).getCity());
        }
    }

    /**
     * Setup class weather info
     */
    private synchronized void prepareWeatherData() {
        if ( !isBinded) return;
        CityID[] otherPlaces = userSettings.getOtherPacesList();
        mCityWeatherList = new ArrayList<>(otherPlaces.length);

        for ( CityID city: otherPlaces ) {
            CityWeatherSettings cs = new CityWeatherSettings(city,
                    Objects.requireNonNull(openWeatherOrgService).getWeatherFor(city),
                    userSettings.getOptions());
            cs.addWeekForecastWeather(Objects.requireNonNull(openWeatherOrgService).getWeatherWeekForecastFor(city));
            mCityWeatherList.add(cs);
        }

        currentCityWeather = new CityWeatherSettings(userSettings.getCurrentPlace(),
                Objects.requireNonNull(openWeatherOrgService).getWeatherFor(userSettings.getCurrentPlace()),
                userSettings.getOptions());
    }

    private synchronized void restoreSettings() {
        CityID[] otherPlaces = userSettings.getOtherPacesList();
        mCityWeatherList = new ArrayList<>(otherPlaces.length);

        for ( CityID city: otherPlaces ) {
            CityWeatherSettings cs = new CityWeatherSettings(city,
                    null,
                    userSettings.getOptions());
            mCityWeatherList.add(cs);
        }

        currentCityWeather = new CityWeatherSettings(userSettings.getCurrentPlace(),
                null,
                userSettings.getOptions());
    }


    private void updateWeatherData() {
        if (!isBinded) return;
        currentCityWeather.setWeather(Objects.requireNonNull(openWeatherOrgService).getWeatherFor(currentCityWeather.getCity()));
        for (CityWeatherSettings cs : mCityWeatherList) {
            cs.setWeather(Objects.requireNonNull(openWeatherOrgService).getWeatherFor(cs.getCity()));
            cs.addWeekForecastWeather(Objects.requireNonNull(openWeatherOrgService).getWeatherWeekForecastFor(cs.getCity()));
        }
    }

    private void bindOpenWeatherService() {
        if (!isBinded) {
            Intent intent = new Intent(this, OpenWeatherOrgService.class);
            bindService(intent, openWeatherOrgServiceConnection, BIND_AUTO_CREATE);
        }
    }

    private void unbindOpenWeatherService() {
        if (isBinded) {
            unbindService(openWeatherOrgServiceConnection);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDebug("onDestroy");
        unbindOpenWeatherService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(onCitySearchResultReceiver, new IntentFilter(OpenWeatherOrgService.BROADCAST_ACTION_SEARCH_FINISHED));
        registerReceiver(onWeatherUpdateReceiver, new IntentFilter(OpenWeatherOrgService.BROADCAST_ACTION_WEATHER_UPDATE_FINISHED));
        onDebug("onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(onCitySearchResultReceiver);
        unregisterReceiver(onWeatherUpdateReceiver);
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
                return true;
            case R.id.actionAdd:
                searchCity();
                return true;
            case R.id.actionRefresh:
                //Refresh all cities
                if (lastSelectedSection == R.id.nav_favorites ) {
                    refreshWeatherDataForOtherPlaces();
                } else {
                    //Refresh current city only
                    if (isBinded) {
                        Objects.requireNonNull(openWeatherOrgService).refreshWeatherDataFor(currentCityWeather.getCity());
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onStorePlacesInUserSettings() {
        //Store all current places in user settings
        userSettings.setCurrentPlace(currentCityWeather.getCity());

        CityID[] otherPlaces = new CityID[mCityWeatherList.size()];

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
        WeatherAppBus.getBus().unregister(this);
        onDebug("onPause");
    }

    /**
     * Activity on resume stuff
     */
    @Override
    protected void onResume() {
        super.onResume();
        //To receive events on WeatherProvider
        WeatherAppBus.getBus().register(this);
        //Startup view
        navViewDisplayCity();
        if (getSupportFragmentManager().getBackStackEntryCount() == 0 ) {
            navigateTo(R.id.nav_weather_details);
        }
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