package com.example.weather.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class WeatherDisplayFragment extends Fragment {
    private TextView cityView;
    private TextView dateTimeView;
    private TextView temperatureView;
    private TextView feelsLikeView;
    private TextView cloudinessView;
    private TextView windView;
    private TextView pressureView;
    private ImageView weatherView;
    private CityWeatherSettings settings;
    private BroadcastReceiver dateTimeChangedReceiver;

    private RecyclerView weatherWeekDetails;

    public static final String WeatherDisplayOptionsKey = "DisplayOptionsKey";

    private static final String WEATHER_ICON_SERVER_MASK = "http://openweathermap.org/img/wn/%s@2x.png";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        settings = new CityWeatherSettings();
        settings.setWeather(null);
        setRetainInstance(true);
        //Extract startup settings if possible
        if (getArguments() != null ) {
            CityWeatherSettings injectedSettings = (CityWeatherSettings)getArguments().getSerializable(WeatherDisplayOptionsKey);
            if ( injectedSettings != null ) {
                settings = injectedSettings;
            }
        }
        return inflater.inflate(R.layout.current_weather_frame, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Check for saved settings and extract it if exists
        if ( savedInstanceState != null ) {
            CityWeatherSettings restoredSettings = (CityWeatherSettings)savedInstanceState.getSerializable(WeatherDisplayOptionsKey);
            if ( restoredSettings != null ) {
                settings = restoredSettings;
            }
        }
        findViews(view);
        updateDate();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupDateTimeViewOnClick();
        setupTemperatureViewOnClick();
        setupWeatherWeekList();
        updateViews();
    }

    /**
     * Activity save instance state
     * @param outState - saved instance state
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(WeatherDisplayOptionsKey, settings);
        super.onSaveInstanceState(outState);
    }

    /**
     * Display new weather
     */
    public void setWeather(@Nullable WeatherEntity weather) {
        settings.setWeather(weather);
        updateViews();
    }

    /**
     * Activity on pause stuff
     */
    @Override
    public void onPause() {
        super.onPause();
        disableDateTimeUpdate();
    }

    /**
     * Activity on resume stuff
     */
    @Override
    public void onResume() {
        super.onResume();
        initDateTimeUpdate();
    }

    private void findViews(View view) {
        dateTimeView = view.findViewById( R.id.dateView );
        temperatureView = view.findViewById( R.id.tempView );
        feelsLikeView = view.findViewById( R.id.feelsLike );
        weatherView = view.findViewById( R.id.avatar);
        cloudinessView = view.findViewById( R.id.cloudinessView);
        windView = view.findViewById(R.id.windView);
        pressureView = view.findViewById(R.id.pressureView);
        cityView = view.findViewById(R.id.current_place);
        weatherWeekDetails = view.findViewById(R.id.weather_week_details);
    }

    /**
     * Setup weathers hourly list
     */
    private void setupWeatherWeekList() {
        RecyclerView.LayoutManager lm = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);
        weatherWeekDetails.setLayoutManager(lm);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(requireContext(),  LinearLayoutManager.HORIZONTAL);
        itemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(),R.drawable.recycleview_separator)));
        weatherWeekDetails.addItemDecoration(itemDecoration);

        WeatherWeekDetailsAdapter wHAdapter = new WeatherWeekDetailsAdapter(settings.getWeekForecast());
        weatherWeekDetails.setAdapter(wHAdapter);
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
        if ( settings.getWeather() != null ) {
            String currentTemperature = Integer.toString(settings.getWeather().getTemperature());
            if ( !settings.getWeather().isFahrenheitTempUnit() ) {
                currentTemperature += getString(R.string.temp_unit_celsius);
            } else {
                currentTemperature += getString(R.string.temp_unit_fahrenheit);
            }
            temperatureView.setText(currentTemperature);
        } else {
            temperatureView.setText(R.string.not_avaliable);
        }
    }

    private void updateCurrentCity() {
        if ( settings.getCity() != null && settings.getCity().getName().length() > 0) {
            cityView.setText(settings.getCity().getName());
        } else {
            cityView.setText(R.string.not_avaliable);
        }
    }

    /**
     * Update weather feels like status
     */
    private void updateFeelsLikeTempView() {
        if ( settings.getWeatherDisplayOptions().isShowFeelsLike() ) {
            feelsLikeView.setVisibility(View.VISIBLE);

            if ( settings.getWeather() != null ) {
                String tempUnit = getString(R.string.temp_unit_celsius);
                if ( settings.getWeather().isFahrenheitTempUnit() ) {
                    tempUnit = getString(R.string.temp_unit_fahrenheit);
                }
                feelsLikeView.setText(String.format("%s %s%s", getString(R.string.feels_like), settings.getWeather().getFeelsLikeTemp(), tempUnit));
            } else {
                feelsLikeView.setText(R.string.not_avaliable);
            }
        } else {
            feelsLikeView.setVisibility(View.GONE);
        }
    }

    /**
     * Update all changeable views
     */
    private void updateViews() {
        updateTemp();
        updateFeelsLikeTempView();
        updateCloudinessView();
        updateWeatherView();
        updateWindView();
        updatePressureView();
        updateCurrentCity();
    }

    /**
     * Setup weather view
     */
    private void updateWindView() {
        if (settings.getWeatherDisplayOptions().isShowWindSpeed()) {
            windView.setVisibility(View.VISIBLE);
            if ( settings.getWeather() != null ) {
                windView.setText(String.format(Locale.getDefault(), "%.1f m/s", settings.getWeather().getWindSpeed()));
            } else {
                windView.setText(R.string.not_avaliable);
            }
        } else {
            windView.setVisibility(View.GONE);
        }
    }

    /**
     * Update pressure view
     */
    private void updatePressureView() {
        if (settings.getWeatherDisplayOptions().isShowPressure()) {
            pressureView.setVisibility(View.VISIBLE);

            if ( settings.getWeather() != null ) {
                pressureView.setText(String.format(Locale.getDefault(), "%d kPa", settings.getWeather().getPressureBar()));
            } else {
                pressureView.setText(R.string.not_avaliable);
            }

        } else {
            pressureView.setVisibility(View.GONE);
        }
    }

    /**
     * Update weather cloudiness status
     */
    private void updateCloudinessView() {
        if ( settings.getWeather() != null ) {
            cloudinessView.setText(settings.getWeather().getCloudiness());
        } else {
            cloudinessView.setText(R.string.not_avaliable);
        }
    }

    /**
     * Update Weather status image
     */
    private void updateWeatherView() {
        if ( settings.getWeather() != null ) {
            if ( settings.getWeatherDisplayOptions().isUseBuildInIcons() ) {
                weatherView.setImageResource(settings.getWeather().getBuildInIconID());
                return;
            }
            Picasso.get().load(String.format(Locale.getDefault(),
                    WEATHER_ICON_SERVER_MASK, settings.getWeather().getExternalIconID()))
                    .placeholder(R.mipmap.ic_weather_na).into(weatherView);
        } else {
            weatherView.setImageResource(R.mipmap.ic_weather_na);
        }
    }

    /**
     * Setup On this day history
     */
    private void setupDateTimeViewOnClick() {
        dateTimeView.setOnClickListener(view -> {
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("MMMM/d", Locale.getDefault());
            String url = getString(R.string.on_this_day) + df.format(currentDate).toLowerCase();
            Uri uri = Uri.parse(url);
            Intent onThisDayBrowser = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(onThisDayBrowser);
        });
    }

    private void setupTemperatureViewOnClick() {
        temperatureView.setOnClickListener(view -> {
            String url = String.format(getString(R.string.weather_yandex), settings.getCity());
            Uri uri = Uri.parse(url.toLowerCase());
            Intent weatherBrowser = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(weatherBrowser);
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
        requireActivity().registerReceiver(dateTimeChangedReceiver, dateTimeUpdateFilter);
    }

    /**
     * Disable auto update date-time view
     */
    private void disableDateTimeUpdate() {
        requireActivity().unregisterReceiver(dateTimeChangedReceiver);
    }
}
