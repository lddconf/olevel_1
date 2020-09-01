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
import java.util.Calendar;
import java.util.Locale;

public class WeatherWeekDetailsAdapter extends RecyclerView.Adapter<WeatherWeekDetailsAdapter.WeatherWeekDetailsViewHolder> {
    private ArrayList<WeatherEntity> weatherForecast;

    public class WeatherWeekDetailsViewHolder extends RecyclerView.ViewHolder {
        private TextView hourView;
        private ImageView cloudiness;
        private TextView tempView;
        private View view;

        public WeatherWeekDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            findViews(itemView);

        }

        private void findViews(View view) {
            hourView = view.findViewById(R.id.dayOfWeekDetailsName);
            cloudiness = view.findViewById(R.id.dayOfWeekDetailsCloudiness);
            tempView = view.findViewById(R.id.dayOfWeekDetailsTemp);
        }

        protected void bind(int position) {
            WeatherEntity w = weatherForecast.get(position);
            final Calendar cal = Calendar.getInstance(Locale.getDefault());
            cal.add(Calendar.DATE, position + 1);
            hourView.setText( cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) );

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

    public WeatherWeekDetailsAdapter(@NonNull ArrayList<WeatherEntity> weatherForecast) {
        this.weatherForecast = weatherForecast;
    }

    public void setWeatherForecast(@NonNull ArrayList<WeatherEntity> weatherForecast) {
        this.weatherForecast = weatherForecast;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WeatherWeekDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hourly_details_cell, parent, false);
        return new WeatherWeekDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherWeekDetailsViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return weatherForecast == null ? 0 : weatherForecast.size();
    }
}
