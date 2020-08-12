package com.example.weather;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.weather.CityWeatherSettings;

import java.util.ArrayList;

public class CitySelectionFragment extends Fragment {
    private RecyclerView cityList;

    private CityListAdapter adapter;
    private ArrayList<CityWeatherSettings> weatherSettingsArrayList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.city_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);
        weatherSettingsArrayList = new ArrayList<>();
        setupCityList();
    }

    private void setupCityList() {
        cityList.setHasFixedSize(true);
        RecyclerView.LayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        cityList.setLayoutManager(layout);
        adapter = new CityListAdapter(getContext(), weatherSettingsArrayList,
                CityListAdapter.DISPLAY_TEMP_MODE | CityListAdapter.DISPLAY_SELECTION_MODE);
        cityList.setAdapter(adapter);
    }

    private void findViews(View view) {
        cityList = view.findViewById(R.id.city_list);
    }



    /**
     * Setup item selected callback. Previous will be erased
     * @param callBack new callback class
     */
    public void setOnItemSelectedCallBack( OnItemClickListener callBack) {
        adapter.setOnItemSelectedCallBack(callBack);
    }

    /**
     * Get selected current
     * @return Selected city
     */
    @Nullable
    public String getSelectedCity() {
        if ( adapter.getSelectedItemIndex() >= 0 ) {
            return weatherSettingsArrayList.get(adapter.getSelectedItemIndex()).getCurrentCity();
        }
        return null;
    }

    /**
     * Set selected index
     * @param index selected index
     */
    public void setItemSelected(int index) {

        if ( index >= 0 ) {
            cityList.scrollToPosition(index);
        }
        adapter.setSelectedItemIndex(index);
    }

    /**
     * Set data array
     * @param array new data array
     */
    public void setWeatherSettingsArray(@NonNull ArrayList<CityWeatherSettings> array) {
        weatherSettingsArrayList = array;
        adapter.setWeatherSet(weatherSettingsArrayList);
    }

    public void displayTemperature(boolean enabled) {
        if ( enabled ) {
            adapter.setViewMode(adapter.getViewMode() | CityListAdapter.DISPLAY_TEMP_MODE );
        } else {
            adapter.setViewMode(adapter.getViewMode() & (~CityListAdapter.DISPLAY_TEMP_MODE) );
        }
    }

    public void enableSelection(boolean enabled) {
        if ( enabled ) {
            adapter.setViewMode(adapter.getViewMode() | CityListAdapter.DISPLAY_SELECTION_MODE );
        } else {
            adapter.setViewMode(adapter.getViewMode() & (~CityListAdapter.DISPLAY_SELECTION_MODE) );
        }
    }

}
