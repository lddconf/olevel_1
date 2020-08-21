package com.example.weather.weather;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weather.R;

import static com.example.weather.weather.WeatherDisplayFragment.WeatherDisplayOptionsKey;

public class WeatherDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ) {
            finish();
            return;
        }

        CityWeatherSettings injectedSettings = (CityWeatherSettings) getIntent().getSerializableExtra(WeatherDisplayOptionsKey);
        if ( injectedSettings != null) {
            setTheme(injectedSettings.getWeatherDisplayOptions().getThemeId());
        }
        setContentView(R.layout.weather_display_activity);

        WeatherDisplayFragment fragment = new WeatherDisplayFragment();
        fragment.setArguments(getIntent().getExtras());

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.weather_details, fragment)
                .commit();
    }
}
