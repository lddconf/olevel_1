package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView cityView;
    private TextView dateTimeView;
    private TextView temperatureView;
    private TextView feelsLikeView;
    private TextView cloudinessView;
    private ImageView weatherView;
    private final int DATE_TIME_UPDATE_MS = 1000;
    private Handler handlerDateTime;
    private Runnable dateTimeUpdater;

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
        initDateTimeUpdate();
    }

    @Override
    protected void onPause() {
        handlerDateTime.removeCallbacks(dateTimeUpdater);
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        handlerDateTime.postDelayed(dateTimeUpdater, DATE_TIME_UPDATE_MS);
    }

    private void initDateTimeUpdate() {
        if ( handlerDateTime == null) {
            handlerDateTime = new Handler();
        }

        if ( dateTimeUpdater == null ) {
            dateTimeUpdater = new Runnable() {
                @Override
                public void run() {
                    updateDate();
                    handlerDateTime.postDelayed(this, DATE_TIME_UPDATE_MS);
                }
            };
        }
        handlerDateTime.removeCallbacks(dateTimeUpdater);
        handlerDateTime.postDelayed(dateTimeUpdater, DATE_TIME_UPDATE_MS);
    }

    private void updateLocation() {
        cityView.setText("Moscow");
    }

    private void updateDate() {
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("E, dd MMM HH:mm", Locale.getDefault());
        String dateTime = df.format(currentDate);
        dateTimeView.setText(dateTime);
    }

    private void updateTemp() {
        int temp = 13; //градусов цельсия
        String currentTemperature = Integer.toString(temp) + getString(R.string.temperature_unit);
        temperatureView.setText(currentTemperature);
    }

    private void updateFeelsLikeTempView() {
        int feelsLikeTemp = 11;
        feelsLikeView.setText(getString(R.string.feels_like) + " " + Integer.toString(feelsLikeTemp) + getString(R.string.temperature_unit));
    }

    private void updateCloudinessView() {
        cloudinessView.setText(getString(R.string.cloudly));
    }

    private void updateWeatherView() {
        weatherView.setImageResource(R.mipmap.ic_cloudly);
    }

    private void initViews() {
        cityView = findViewById( R.id.cityView );
        dateTimeView = findViewById( R.id.dateView );
        temperatureView = findViewById( R.id.tempView );
        feelsLikeView = findViewById( R.id.feelsLike );
        weatherView = findViewById( R.id.imageView);
        cloudinessView = findViewById( R.id.cloudinessView);
    }
}