package com.example.weather;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.weather.CityWeatherSettings;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

public class CitySelectionFragment extends Fragment {
    private RecyclerView cityList;
    private OnCityViewFragmentEvent onCityViewFragmentEvent;
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
        setRetainInstance(true);

        findViews(view);
        weatherSettingsArrayList = new ArrayList<>();
        setupCityList();

        if ( onCityViewFragmentEvent != null ) {
            onCityViewFragmentEvent.onCityViewFragmentCreated();
        }
    }

    private void setupCityList() {
        Context context = requireContext();

        cityList.setHasFixedSize(true);
        RecyclerView.LayoutManager layout = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        cityList.setLayoutManager(layout);


        DividerItemDecoration itemDecoration = new DividerItemDecoration(context,  LinearLayoutManager.VERTICAL);
        itemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(context,R.drawable.recycleview_separator)));
        cityList.addItemDecoration(itemDecoration);

        adapter = new CityListAdapter(getContext(), weatherSettingsArrayList,
                CityListAdapter.DISPLAY_TEMP_MODE | CityListAdapter.DISPLAY_SELECTION_MODE);
        cityList.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(adapter.new CityListSwipeToDeleteCallback(adapter));
        itemTouchHelper.attachToRecyclerView(cityList);

        adapter.setOnItemDeletedListener(() -> {
            View view = cityList;
            Snackbar snackbar = Snackbar.make(view, R.string.city_deleted_undo_snackbar_txt,
                    Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.undo_city_delete_action, v->{
                adapter.undoLastDeleted();
            });
            snackbar.show();
        });
    }

    private void findViews(View view) {
        cityList = view.findViewById(R.id.city_list);
    }


    public interface OnCityViewFragmentEvent {
        void onCityViewFragmentCreated();
    }

    public void setOnCityViewFragmentEvent(final OnCityViewFragmentEvent onCityViewFragmentEvent) {
        this.onCityViewFragmentEvent = onCityViewFragmentEvent;
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
            return weatherSettingsArrayList.get(adapter.getSelectedItemIndex()).getCity();
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
