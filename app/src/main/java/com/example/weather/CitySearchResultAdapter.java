package com.example.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.weatherprovider.openweatherorg.OpenWeatherSearchResultEvent;

import java.util.ArrayList;
import java.util.Locale;

public class CitySearchResultAdapter extends RecyclerView.Adapter<CitySearchResultAdapter.CityViewHolder> {
    private Context context;
    private ArrayList<OpenWeatherSearchResultEvent.WeatherSearchDetails> searchResults;
    private CitySelectionDialogFragment.OnCitySelectedListener onItemClickListener;
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
        void bind( final OpenWeatherSearchResultEvent.WeatherSearchDetails weatherSettings ) {
            cityNameView.setText( String.format(Locale.getDefault(), "%s, [%s]", weatherSettings.getName(), weatherSettings.getCountry()));

            /*
            String tempUnit = context.getString(R.string.not_avaliable);
            briefTempView.setText(tempUnit);
            */

            cityNameView.setOnClickListener(view -> handleOnItemClick());
            briefTempView.setOnClickListener(v -> handleOnItemClick());

        }

        private void handleOnItemClick() {
            if (checkedPosition != getAdapterPosition()) {
                notifyItemChanged(checkedPosition); //For uncheck previous
                checkedPosition = getAdapterPosition();
                notifyItemChanged(checkedPosition); //For uncheck previous
            }
            if (onItemClickListener != null) {
                if ( checkedPosition >= 0 ) {
                    onItemClickListener.onCitySelected(searchResults.get(checkedPosition).getCityID());
                } else {
                    onItemClickListener.onCitySelected(null);
                }
            }
        }

        private void findViews(View view) {
            cityNameView = view.findViewById(R.id.cityName);
            briefTempView = view.findViewById(R.id.briefTemp);
        }
    }

    /**
     * Object constructor
     * @param searchResults - cities weather array set
     */
    public CitySearchResultAdapter(Context context, @NonNull ArrayList<OpenWeatherSearchResultEvent.WeatherSearchDetails> searchResults, int mode ) {
        this.context = context;
        this.searchResults = searchResults;
        this.viewMode = mode;
        onItemClickListener = null;
    }

    /**
     * Setup item selected callback. Previous will be erased
     * @param callBack new callback class
     */
    public void setOnCitySelectedCallBack(CitySelectionDialogFragment.OnCitySelectedListener callBack) {
        onItemClickListener = callBack;
    }

    /**
     * Setup new weather set
     * @param mWeatherSet new weather set
     */
    public void setWeatherSet(@NonNull ArrayList<OpenWeatherSearchResultEvent.WeatherSearchDetails> mWeatherSet) {
        this.searchResults = mWeatherSet;
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
        int lastCheckedPosition = checkedPosition;
        if (index >= 0 && index < getItemCount() ) {
            checkedPosition = index;
        } else {
            checkedPosition = -1;
        }

        notifyItemChanged(lastCheckedPosition); //For uncheck previous

        if ( checkedPosition >= 0 ) {
            notifyItemChanged(checkedPosition); //For check new
        }
        if ( onItemClickListener != null ) {
            if ( checkedPosition >= 0) {
                onItemClickListener.onCitySelected(searchResults.get(checkedPosition).getCityID());
            } else {
                onItemClickListener.onCitySelected(null);
            }
        }
    }

    /**
     * Inflate the item layout and create the holder
     * @param parent parent view
     * @param viewType ignored
     * @return created view holder entity
     */
    @NonNull
    @Override
    public CitySearchResultAdapter.CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_search_info_layout, parent, false);
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
        holder.bind(searchResults.get(position));
    }

     /**
     * Get item count
     * @return item count
     */
    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public void setViewMode(int mode) {
        viewMode = mode;
        notifyDataSetChanged();
    }

    public int getViewMode() {
        return viewMode;
    }
}
