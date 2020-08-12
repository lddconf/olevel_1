package com.example.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.weather.CityWeatherSettings;

import java.util.ArrayList;

public class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.CityViewHolder> {
    private Context context;
    private ArrayList<CityWeatherSettings> mWeatherSet;
    private OnItemClickListener onItemClickListener;
    private int checkedPosition = -1;
    private int viewMode;

    public static int DISPLAY_TEMP_MODE = 0x1;
    public static int DISPLAY_SELECTION_MODE = 0x2;
    /**
     * Reference views for each data item
     */
    public class CityViewHolder extends RecyclerView.ViewHolder {
        private TextView cityNameView;
        private TextView briefTempView;
        private int mode;

        public CityViewHolder(View view, int mode) {
            super(view);
            this.mode = mode;
            findViews(view);
            if ( (mode & DISPLAY_TEMP_MODE) == 0 ) {
                briefTempView.setVisibility(View.GONE);
            }
        }

        /**
         * Bind new data to view
         * @param weatherSettings displayed weather settings
         */
        void bind( final CityWeatherSettings weatherSettings ) {
            if ( checkedPosition == -1 ) {
                cityNameView.setTextColor(ContextCompat.getColor(context, R.color.colorText));
                briefTempView.setTextColor(ContextCompat.getColor(context, R.color.colorText));
            } else {
                if (checkedPosition == getAdapterPosition()) {
                    if ( (mode & DISPLAY_SELECTION_MODE) > 0 ) {
                        cityNameView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
                        briefTempView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
                    }
                } else {
                    cityNameView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackground));
                    briefTempView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackground));
                    //cityNameView.setTextColor(ContextCompat.getColor(context, R.color.colorText));
                }
            }

            cityNameView.setText(weatherSettings.getCurrentCity());

            String tempUnit = Integer.toString(weatherSettings.getWeather().getTemperature());
            if (weatherSettings.getWeather().isFahrenheitTempUnit()) {
                tempUnit += context.getString(R.string.temp_unit_fahrenheit);
            } else {
                tempUnit += context.getString(R.string.temp_unit_celsius);
            }
            briefTempView.setText(tempUnit);

            cityNameView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkedPosition != getAdapterPosition()) {
                            notifyItemChanged(checkedPosition); //For uncheck previous
                            checkedPosition = getAdapterPosition();
                            notifyItemChanged(checkedPosition); //For uncheck previous
                            if (onItemClickListener != null) {
                                onItemClickListener.onItemClick(checkedPosition);
                            }
                        }
                    }
                });
        }

        private void findViews(View view) {
            cityNameView = view.findViewById(R.id.cityName);
            briefTempView = view.findViewById(R.id.breafTemp);
        }
    }

    /**
     * Object constructor
     * @param mWeatherSet - cities weather array set
     */
    public CityListAdapter( Context context, @NonNull ArrayList<CityWeatherSettings> mWeatherSet, int mode ) {
        this.context = context;
        this.mWeatherSet = mWeatherSet;
        this.viewMode = mode;
        onItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(int id) {
                //Nothing to do
            }
        };
    }

    /**
     * Setup item selected callback. Previous will be erased
     * @param callBack new callback class
     */
    public void setOnItemSelectedCallBack( OnItemClickListener callBack) {
        onItemClickListener = callBack;
    }

    /**
     * Setup new weather set
     * @param mWeatherSet new weather set
     */
    public void setWeatherSet(@NonNull ArrayList<CityWeatherSettings> mWeatherSet) {
        this.mWeatherSet = mWeatherSet;
        notifyDataSetChanged();
    }

    /**
     * Get index of selected item
     * @return index of selected item
     */
    public int getSelectedItemIndex() {
        return checkedPosition;
    }

    public void setSelectedItemIndex(int index) {
        notifyItemChanged(checkedPosition); //For uncheck previous
        if (index >= 0 && index < getItemCount() ) {
            checkedPosition = index;
        } else {
            checkedPosition = -1;
        }
        notifyItemChanged(checkedPosition); //For check new
        onItemClickListener.onItemClick(checkedPosition);
    }

    /**
     * Inflate the item layout and create the holder
     * @param parent parent view
     * @param viewType ignored
     * @return created view holder entity
     */
    @NonNull
    @Override
    public CityListAdapter.CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_info_layout, parent, false);
        return new CityViewHolder(view, viewMode);
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     * @param holder element
     * @param position position
     */
    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        // - get element from your data set at this position
        // - replace the contents of the view with that element
        holder.bind(mWeatherSet.get(position));
    }

    /**
     * Get item count
     * @return item count
     */
    @Override
    public int getItemCount() {
        return mWeatherSet.size();
    }

    public void setViewMode(int mode) {
        viewMode = mode;
        notifyDataSetChanged();
    }

    public int getViewMode() {
        return viewMode;
    }
}
