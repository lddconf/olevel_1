package com.example.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.diplayoption.WeatherDisplayOptionsFragment;
import com.example.weather.history.WeatherCity;
import com.example.weather.history.WeatherHistory;
import com.example.weather.history.WeatherIcon;
import com.example.weather.history.view.WeatherHistoryList;
import com.example.weather.weather.CityWeatherSettings;
import com.example.weather.weather.WeatherDisplayFragment;
import com.example.weather.weather.WeatherEntity;
import com.example.weather.weatherprovider.OpenWeatherOrgService;
import com.example.weather.weatherprovider.openweatherorg.OpenWeatherProviderEvent;
import com.example.weather.weatherprovider.openweatherorg.OpenWeatherSearchResultEvent;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Main Weather Info Activity
 */
public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0x5151;
    private static final double LOCATION_NOT_INIT = -180;
    private static final int GOOGLE_SIGN_IN = 0x4422;

    private Toolbar mainToolBar;

    private com.google.android.gms.common.SignInButton buttonSignIn;
    private GoogleSignInClient googleSignInClient;
    private TextView userName;
    private TextView userEmail;
    private ImageView avatar;
    private MenuItem logoutItem;

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

    private static final String mainActivityLastLongitudeKey = "AppMainActivityLastLongitudeKey";
    private static final String mainActivityLastLatitudeKey = "AppMainActivityLastLatitudeKey";

    public static final String mainActivityViewOptionsKey = "AppMainActivityViewOptions";

    private static final int settingsChangedRequestCode = 0x1;
    private static final boolean debug = false;

    private UserSettings userSettings;
    private CitySearchDialog searchDialog;

    private boolean isBinded = false;
    private OpenWeatherOrgService.OpenWeatherOrgBinder openWeatherOrgService;

    private WifiStateChangedReceiver wifiStateChangedReceiver;

    private double lastLongitude;
    private double lastLatitude;

    private ServiceConnection openWeatherOrgServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            openWeatherOrgService = (OpenWeatherOrgService.OpenWeatherOrgBinder) iBinder;
            isBinded = true;

            if ( BuildConfig.FLAVOR.equals("with_geolocation" )) {
                //Refresh data for current city
                if (Objects.requireNonNull(userSettings).isUseGPSTracing() || currentCityWeather.getCity() == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (lastLongitude != LOCATION_NOT_INIT) {
                                openWeatherOrgService.findForecastFor(lastLatitude, lastLongitude);
                            }
                        }
                    });
                } else {
                    openWeatherOrgService.refreshWeatherDataFor(currentCityWeather.getCity());
                }
            } else {
                openWeatherOrgService.refreshWeatherDataFor(currentCityWeather.getCity());
            }
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

        lastLongitude = LOCATION_NOT_INIT;
        lastLatitude  = LOCATION_NOT_INIT;

        userSettings = UserSettings.getUserSettings();
        loadPreferences();

        restoreSettingsFromBundle(savedInstanceState);

        setTheme(userSettings.getOptions().getThemeId());
        setContentView(R.layout.activity_nav);

        findViews();

        bindOpenWeatherService();

        if (mCityWeatherList == null) {

            //For fist run build data from old state
            restoreSettings();
        }

        setupActionBar();
        setupNavigationDrawer();
        setOnClickForSideMenuItems();
        setupWifiStateChangedMonitor();

        if ( BuildConfig.FLAVOR.equals("with_geolocation" )) {
            checkGeoPermissions();
        }

        setupGoogleAuthentication();

        lastSelectedSection = -1;
        searchDialog = new CitySearchDialog();
        onDebug("onCreate");
    }

    private void setupGoogleAuthentication() {
        // Конфигурация запроса на регистрацию пользователя, чтобы получить
        // идентификатор пользователя, его почту и основной профайл
        // (регулируется параметром)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Получаем клиента для регистрации и данные по клиенту
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

    }

    // Инициируем регистрацию пользователя
    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    private void signOut() {
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                enableSingIn();
                navigationView.setCheckedItem(lastSelectedSection);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            // Когда сюда возвращается Task, результаты аутентификации уже
            // готовы
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            processSignInResult(task);
        }

    }

    private void processSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            disableSignIn();
            updateNavHeader(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure
            // reason. Please refer to the GoogleSignInStatusCodes class
            // reference for more information.
        }
    }

    private void updateNavHeader(final GoogleSignInAccount account) {
        if ( account != null ) {
            userName.setText(account.getDisplayName());
            userEmail.setText(account.getEmail());
            Picasso.get().load(account.getPhotoUrl()).placeholder(R.mipmap.ic_launcher).transform(new CircleTransform()).into(avatar);
        }
    }

    private void enableSingIn() {
        userName.setVisibility(View.GONE);
        userEmail.setVisibility(View.GONE);
        avatar.setVisibility(View.GONE);
        buttonSignIn.setVisibility(View.VISIBLE);
        logoutItem.setVisible(false);
    }

    private void disableSignIn() {
        userName.setVisibility(View.VISIBLE);
        userEmail.setVisibility(View.VISIBLE);
        avatar.setVisibility(View.VISIBLE);
        buttonSignIn.setVisibility(View.GONE);
        logoutItem.setVisible(true);
    }

    private void savePreferences() {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        userSettings.setCurrentPlace(currentCityWeather.getCity());

        for (int i = 0; i < mCityWeatherList.size(); i++) {
            if ( !mCityWeatherList.get(i).equals(currentCityWeather) ) {
                userSettings.addOtherPlace(Objects.requireNonNull(mCityWeatherList.get(i)).getCity());
            }
        }


        Gson gson = new Gson();
        String options = gson.toJson(userSettings);
        editor.putString(mainActivityViewOptionsKey, options);
        editor.apply();
    }

    private void loadPreferences() {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        String usettings = sharedPref.getString(mainActivityViewOptionsKey, "");
        if (usettings.length() > 0) {
            Gson gson = new Gson();
            UserSettings loadedUserSettings = gson.fromJson(usettings, UserSettings.class);
            if (loadedUserSettings != null) {
                userSettings = loadedUserSettings;
            }
        }
    }

    private void setupNavigationDrawer() {
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_favorites, R.id.nav_about, R.id.nav_weather_details, R.id.nav_feedback, R.id.nav_history)
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
                    if (index < mCityWeatherList.size()) {
                        if (index >= 0) {
                            CityWeatherSettings wsettings = mCityWeatherList.get(index);
                            mCityWeatherList.remove(index);
                            mCityWeatherList.add(0, currentCityWeather);
                            currentCityWeather = wsettings;
                            userSettings.setUseGPSTracing(false);
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
        String city = getString(R.string.not_avaliable);
        if (currentCityWeather.getCity() != null ) {
            city = currentCityWeather.getCity().getName();
        }

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
        if (currentCityWeather.getWeather() == null) {
            if (isBinded) {
                Objects.requireNonNull(openWeatherOrgService).refreshWeatherDataFor(currentCityWeather.getCity());
            }
        }
    }

    private void setHistory() {

        setFragment(WeatherHistoryList.newInstance());
    }

    private void setSettingsFragment() {
        WeatherDisplayOptionsFragment settings = new WeatherDisplayOptionsFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(WeatherDisplayOptionsFragment.DisplayOptionsKey, userSettings.getOptions());
        settings.setArguments(arguments);
        settings.setOnThemeChangedListener(() -> {
            saveOptionIfNeeded();
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
        if (lastFragment instanceof WeatherDisplayOptionsFragment) {
            userSettings.setOptions(((WeatherDisplayOptionsFragment) lastFragment).getCurrentOptions());
            for (CityWeatherSettings w : mCityWeatherList) {
                w.setWeatherDisplayOptions(userSettings.getOptions());
            }
            currentCityWeather.setWeatherDisplayOptions(userSettings.getOptions());
            savePreferences();
        }
    }

    private boolean showNavigateFragment(int id) {
        saveOptionIfNeeded();
        if ( id != R.id.nav_logout ) {
            drawer.closeDrawers();
        }
        if (lastSelectedSection != id) {
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
                    savePreferences();
                    break;
                case R.id.nav_history:
                    setHistory();
                    break;
                case R.id.nav_logout:
                    id = lastSelectedSection;
                    signOut();
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
            final OpenWeatherProviderEvent result = (OpenWeatherProviderEvent) intent.getSerializableExtra(OpenWeatherOrgService.BROADCAST_ACTION_WEATHER_UPDATE_RESULT);

            if (result == null) return;
            // Потокобезопасный вывод данных
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onOWeatherEvent(result);
                }
            });
        }
    };

    private void onOWeatherEvent(@NonNull OpenWeatherProviderEvent event) {
        switch (event.getResultCode()) {
            case CONNECTION_ERROR:
                displayMessage(event.getErrorDescription());
                break;
            case REQUEST_COMPLETED_KEYWORD:
                updateWeatherDataFor(event.getCity());
                updateWeatherView();
                break;
        }
    }

    private BroadcastReceiver onCitySearchResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final OpenWeatherSearchResultEvent event
                    = (OpenWeatherSearchResultEvent) intent.getSerializableExtra(OpenWeatherOrgService.BROADCAST_ACTION_SEARCH_RESULT);
            if (event != null) {
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
            case REQUEST_COMPLETED_KEYWORD:
                //Nothing found
                CitySelectionDialogFragment dialogFragment =
                        CitySelectionDialogFragment.newInstance(event.getSearchDetails(), event.getKeyword());
                dialogFragment.show(getSupportFragmentManager(),
                        "dialog_fragment");
                break;
            case REQUEST_COMPLETED_LAT_LONG:
                //Try to add new city
                userSettings.setUseGPSTracing(true);
                if ( event.getSearchDetails().size() > 0 ) {
                    addNewCity(event.getSearchDetails().get(0).getCityID());
                }
                break;
        }
    }

    private void addNewCity(CityID cityID) {
        if ( cityID == null ) return;
        if ( currentCityWeather.getCity() != null ) {
            if (currentCityWeather.getCity().equals(cityID)) {
                if ( currentCityWeather.getWeather() == null ) {
                    Objects.requireNonNull(openWeatherOrgService).refreshWeatherDataFor(currentCityWeather.getCity());
                }
                navigateTo(R.id.nav_weather_details);
                return; //already exist in current place
            }

            //Check selected city for other places
            for (CityWeatherSettings cws : mCityWeatherList
            ) {
                if (cws.getCity().equals(cityID)) {
                    mCityWeatherList.add(0, currentCityWeather);
                    mCityWeatherList.remove(cws);
                    currentCityWeather = cws;
                    if ( currentCityWeather.getWeather() == null ) {
                        Objects.requireNonNull(openWeatherOrgService).refreshWeatherDataFor(currentCityWeather.getCity());
                    }
                    //navViewDisplayCity();
                    navigateTo(R.id.nav_favorites);
                    navigateTo(R.id.nav_weather_details);
                    return;
                }
            }
        }

        //Add new city
        if ( currentCityWeather.getCity() != null && !currentCityWeather.getCity().equals(cityID) ) {
            boolean contains = false;
            for (CityWeatherSettings cws : mCityWeatherList
            ) {
                if (cws.getCity().equals(cityID)) {
                    contains = true;
                }
            }
            if ( !contains ) {
                mCityWeatherList.add(0, currentCityWeather);
            }
        }
        currentCityWeather = new CityWeatherSettings(cityID, new WeatherEntity(), userSettings.getOptions());
        if (isBinded) {
            Objects.requireNonNull(openWeatherOrgService).refreshWeatherDataFor(currentCityWeather.getCity());
        }

        navigateTo(R.id.nav_favorites);
        navigateTo(R.id.nav_weather_details);
    }


    @Subscribe
    public void addNewCity(SearchEngineCitySelectedEvent event) {
        userSettings.setUseGPSTracing(false);
        if (event.getCityID() != null) {
            addNewCity(event.getCityID());
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
        if (lastFragment == null) return;
        if (lastFragment instanceof WeatherDisplayFragment) {
            ((WeatherDisplayFragment) lastFragment).setWeather(currentCityWeather.getWeather());
        }
        if (lastFragment instanceof CitySelectionFragment) {
            ((CitySelectionFragment) lastFragment).setWeatherSettingsArray(mCityWeatherList);
        }
    }

    private void refreshWeatherDataForOtherPlaces() {
        //Request new weather data from internet
        if (!isBinded) return;
        for (int i = 0; i < mCityWeatherList.size(); i++) {
            Objects.requireNonNull(openWeatherOrgService).refreshWeatherDataFor(mCityWeatherList.get(i).getCity());
        }
    }

    /**
     * Setup class weather info
     */
    private synchronized void prepareWeatherData() {
        if (!isBinded) return;
        CityID[] otherPlaces = userSettings.getOtherPacesList();
        mCityWeatherList = new ArrayList<>(otherPlaces.length);

        for (CityID city : otherPlaces) {
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

        for (CityID city : otherPlaces) {
            CityWeatherSettings cs = new CityWeatherSettings(city,
                    null,
                    userSettings.getOptions());
            mCityWeatherList.add(cs);
        }

        currentCityWeather = new CityWeatherSettings(userSettings.getCurrentPlace(),
                null,
                userSettings.getOptions());
    }

    private void logRequestToDataBase(@NonNull CityID cityID, @NonNull WeatherEntity weatherEntity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long cityId = WeatherApp.getInstance().getWeatherCityDAO().getOrMakeCityId(WeatherCity.makeFrom(cityID));
                long iconId = WeatherApp.getInstance().getWeatherIconsDAO().getOrMakeIconId(WeatherIcon.makeFrom(weatherEntity));
                WeatherApp.getInstance().getWeatherHistoryDAO().insertWeatherHistory(WeatherHistory.make(weatherEntity, cityId, iconId));
            }
        }).start();
    }

    private void setupWifiStateChangedMonitor() {
        wifiStateChangedReceiver = new WifiStateChangedReceiver();
    }


    private void updateWeatherDataFor(CityID city) {
        if (!isBinded) return;

        WeatherEntity weather = Objects.requireNonNull(openWeatherOrgService).getWeatherFor(city);

        if (weather != null) {
            logRequestToDataBase(city, weather);
        }
        if (currentCityWeather.getCity().equals(city)) {
            currentCityWeather.setWeather(weather);
        } else {
            for (CityWeatherSettings cs : mCityWeatherList) {
                if (cs.getCity().equals(city)) {
                    cs.setWeather(weather);
                    cs.addWeekForecastWeather(Objects.requireNonNull(openWeatherOrgService).getWeatherWeekForecastFor(cs.getCity()));
                }
            }
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
        registerReceiver(wifiStateChangedReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        enableSingIn();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Пользователь уже входил, сделаем кнопку недоступной
            updateNavHeader(account);
            disableSignIn();
        }

        onDebug("onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(onCitySearchResultReceiver);
        unregisterReceiver(onWeatherUpdateReceiver);
        unregisterReceiver(wifiStateChangedReceiver);
        onDebug("onStop");
    }

    /**
     * Setup action bar
     */
    void setupActionBar() {
        setSupportActionBar(mainToolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private void checkGeoPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            //All ok, can locate
            requestLocation();
        } else {
            requestLocationPermissions();
        }
    }

    //Geolocation permission request
    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                requestLocation();
            }
        }
    }

    private void locationChanged(double latitude, double longitude) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lastLatitude = latitude;
                lastLongitude = longitude;
                if ( isBinded && userSettings.isUseGPSTracing()) {
                    openWeatherOrgService.findForecastFor(latitude, longitude);
                }
            }
        });
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 3000, 10, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    locationChanged(latitude, longitude);
                }

                //Если не переопределить, то runtime ошибка под 21 SDK
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                }
            });
        }
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
                userSettings.setUseGPSTracing(true);
                Objects.requireNonNull(openWeatherOrgService).findForecastFor(lastLatitude, lastLongitude);
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

        outState.putDouble(mainActivityLastLatitudeKey, lastLatitude);
        outState.putDouble(mainActivityLastLongitudeKey, lastLongitude);

        onStorePlacesInUserSettings();

        onDebug("onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    private void restoreSettingsFromBundle(@Nullable Bundle savedInstanceState) {
        try {
            if ( savedInstanceState == null ) return;
            lastSelectedSection = savedInstanceState.getInt(mainActivityLastSelectedSectionKey);
            currentCityWeather = (CityWeatherSettings)savedInstanceState.getSerializable(mainActivityCurrentCityKey);

            lastLatitude = savedInstanceState.getDouble(mainActivityLastLatitudeKey);
            lastLongitude = savedInstanceState.getDouble(mainActivityLastLongitudeKey);

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
        savePreferences();
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

        buttonSignIn = navigationView.getHeaderView(0).findViewById(R.id.sign_in_button);
        userName = navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);
        userEmail = navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);
        avatar = navigationView.getHeaderView(0).findViewById(R.id.nav_header_avatar);



        Menu menuNav = navigationView.getMenu();
        citySelectItem = menuNav.findItem(R.id.nav_weather_details);
        logoutItem = menuNav.findItem(R.id.nav_logout);
        layout = drawer;
    }
}