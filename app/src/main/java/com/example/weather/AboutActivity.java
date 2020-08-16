package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.weather.diplayoption.WeatherDisplayOptions;

import static com.example.weather.MainActivity.mainActivityViewOptionsKey;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        WeatherDisplayOptions options = (WeatherDisplayOptions)getIntent().getSerializableExtra(mainActivityViewOptionsKey);
        if ( options != null ) {
            setTheme(options.getThemeId());
        }

        setContentView(R.layout.activity_about);
    }
}