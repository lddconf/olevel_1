package com.example.weather.weather;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.R;

import java.util.ArrayList;
import java.util.Locale;

public class WHourDetailsAdapter extends RecyclerView.Adapter<WHourDetailsAdapter.WHourDetailsViewHolder> {
    private ArrayList<WeatherEntity> weatherForecast;
    private int baseHour;

    public class WHourDetailsViewHolder extends RecyclerView.ViewHolder {
        private TextView hourView;
        private ImageView cloudiness;
        private TextView tempView;
        private View view;

        public WHourDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            findViews(itemView);

        }

        private void findViews(View view) {
            hourView = view.findViewById(R.id.hourlyDetailsTime);
            cloudiness = view.findViewById(R.id.hourlyDetailsCloudiness);
            tempView = view.findViewById(R.id.hourlyDetailsTemp);
        }

        protected void bind(int position) {
            WeatherEntity w = weatherForecast.get(position);
            hourView.setText( String.format(Locale.getDefault(),"%02d:00", (baseHour+position)%24 ));
            if ( w.getCloudiness().equals(view.getContext().getString(R.string.cloudy))) {
                cloudiness.setImageResource(R.mipmap.ic_cloudly);
            }
            updateTemperature(w);
        }

        private void updateTemperature(WeatherEntity w) {
            String currentTemperature = Integer.toString(w.getTemperature());
            if ( !w.isFahrenheitTempUnit() ) {
                currentTemperature += view.getContext().getString(R.string.temp_unit_celsius);
            } else {
                currentTemperature += view.getContext().getString(R.string.temp_unit_fahrenheit);
            }
            tempView.setText(currentTemperature);
        }
    }

    public WHourDetailsAdapter(@NonNull ArrayList<WeatherEntity> weatherForecast, int baseHour) {
        this.weatherForecast = weatherForecast;
        this.baseHour = baseHour;
    }

    public void setWeatherForecast(@NonNull ArrayList<WeatherEntity> weatherForecast, int baseHour) {
        this.weatherForecast = weatherForecast;
        this.baseHour = baseHour;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WHourDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hourly_details_cell, parent, false);
        return new WHourDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WHourDetailsViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return weatherForecast.size();
    }
}
