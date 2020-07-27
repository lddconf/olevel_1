package com.example.weater;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TextView city;
    private TextView dateTime;
    private TextView temperature;
    private TextView feelsLike;
    private TextView cloudness;
    private ImageView weatherView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        updateLocation();
        updateDate();
        updateTemp();
        updateFeelsLikeTempView();
        updateCloudinessView();
        updateWeatherView();
    }

    private void updateLocation() {
        city.setText("Moscow");
    }

    private void updateDate() {
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("E, dd MMM HH:mm");
        String dtime = df.format(currentDate);
        dateTime.setText(dtime);
    }

    private void updateTemp() {
        int temp = 13; //градусов цельсия
        String currentTemperature = Integer.toString(temp) + getString(R.string.temperature_unit);
        temperature.setText(currentTemperature);
    }

    private void updateFeelsLikeTempView() {
        int feelsLikeTemp = 11;
        feelsLike.setText(getString(R.string.feels_like) + " " + Integer.toString(feelsLikeTemp) + getString(R.string.temperature_unit));
    }

    private void updateCloudinessView() {
        cloudness.setText(getString(R.string.cloudly));
    }

    private void updateWeatherView() {
        weatherView.setImageResource(R.mipmap.ic_cloudly);
    }

    private void initViews() {
        city = findViewById( R.id.cityView );
        dateTime = findViewById( R.id.dateView );
        temperature = findViewById( R.id.tempView );
        feelsLike = findViewById( R.id.feelsLike );
        weatherView = findViewById( R.id.imageView);
        cloudness = findViewById( R.id.cloudinessView);
    }
}