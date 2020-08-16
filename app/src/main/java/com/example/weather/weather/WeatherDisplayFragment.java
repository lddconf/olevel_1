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

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class WeatherDisplayFragment extends Fragment {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        settings = new CityWeatherSettings();
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
            settings = (CityWeatherSettings)savedInstanceState.getSerializable(WeatherDisplayOptionsKey);
        }
        findViews(view);
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
    public void setWeather(@NonNull WeatherEntity weather) {
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
        weatherView = view.findViewById( R.id.imageView);
        cloudinessView = view.findViewById( R.id.cloudinessView);
        windView = view.findViewById(R.id.windView);
        pressureView = view.findViewById(R.id.pressureView);

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
        String currentTemperature = Integer.toString(settings.getWeather().getTemperature());
        if ( !settings.getWeather().isFahrenheitTempUnit() ) {
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
        if ( settings.getWeatherDisplayOptions().isShowFeelsLike() ) {
            feelsLikeView.setVisibility(View.VISIBLE);
            String tempUnit = getString(R.string.temp_unit_celsius);
            if ( settings.getWeather().isFahrenheitTempUnit() ) {
                tempUnit = getString(R.string.temp_unit_fahrenheit);
            }
            feelsLikeView.setText(String.format("%s %s%s", getString(R.string.feels_like), settings.getWeather().getFeelsLikeTemp(), tempUnit));
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
    }

    /**
     * Setup weather view
     */
    private void updateWindView() {
        if (settings.getWeatherDisplayOptions().isShowWindSpeed()) {
            windView.setVisibility(View.VISIBLE);
            windView.setText(String.format(Locale.getDefault(), "%.1f m/s", settings.getWeather().getWindSpeed()));
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
            pressureView.setText(String.format(Locale.getDefault(),"%d mm", settings.getWeather().getPressureBar()));
        } else {
            pressureView.setVisibility(View.GONE);
        }
    }

    /**
     * Update weather cloudiness status
     */
    private void updateCloudinessView() {
        cloudinessView.setText(settings.getWeather().getCloudiness());
    }

    /**
     * Update Weather status image
     */
    private void updateWeatherView() {
        if ( settings.getWeather().getCloudiness().equals(requireContext().getString(R.string.cloudy))) {
            weatherView.setImageResource(R.mipmap.ic_cloudly);
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
                String url = String.format(getString(R.string.weather_yandex), settings.getCurrentCity());
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
        requireActivity().registerReceiver(dateTimeChangedReceiver, dateTimeUpdateFilter);
    }

    /**
     * Disable auto update date-time view
     */
    private void disableDateTimeUpdate() {
        requireActivity().unregisterReceiver(dateTimeChangedReceiver);
    }
}
