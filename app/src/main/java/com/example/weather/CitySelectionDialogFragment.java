package com.example.weather;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.weatherprovider.openweatherorg.OpenWeatherSearchResultEvent;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.LinkedList;

public class CitySelectionDialogFragment extends BottomSheetDialogFragment {
    private ArrayList<OpenWeatherSearchResultEvent.WeatherSearchDetails> searchDetails;
    private String keyword;
    private TextView searchSummary;
    private RecyclerView cityList;
    private CitySearchResultAdapter adapter;

    public static final String searchDetailsKey = "searchDetailsKey";
    public static final String searchKeywordKey = "searchKeywordKey";




    public static CitySelectionDialogFragment newInstance(final LinkedList<OpenWeatherSearchResultEvent.WeatherSearchDetails> searchDetails, final String keyword) {
        CitySelectionDialogFragment fragment = new CitySelectionDialogFragment();
        Bundle args = new Bundle();
        args.putString(searchKeywordKey, keyword);
        args.putSerializable(searchDetailsKey, new ArrayList<>(searchDetails));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            keyword = getArguments().getString(searchKeywordKey);
            searchDetails = (ArrayList<OpenWeatherSearchResultEvent.WeatherSearchDetails>)(getArguments().getSerializable(searchDetailsKey));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_city_selection_dialog, container, false);

        findViews(view);

        setupSearchSummary();
        setupSearchDetails();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void setupSearchSummary() {
        if ( searchDetails == null || searchDetails.size() == 0 ) {
            searchSummary.setVisibility(View.VISIBLE);
            searchSummary.setText(String.format("No results found for \"%s\"", keyword));
            return;
        }
        searchSummary.setVisibility(View.GONE);
    }

    public interface OnCitySelectedListener {
        void onCitySelected( UserSettings.CityID cityID );
    }

    private void setupSearchDetails() {
        if ( searchDetails == null || searchDetails.size() == 0 ) {
            cityList.setVisibility(View.GONE);
            return;
        }
        cityList.setVisibility(View.VISIBLE);

        cityList.setHasFixedSize(true);
        RecyclerView.LayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        cityList.setLayoutManager(layout);

        adapter = new CitySearchResultAdapter(getContext(), searchDetails, CitySearchResultAdapter.DISPLAY_TEMP_MODE);
        cityList.setAdapter(adapter);

        adapter.setOnCitySelectedCallBack(new OnCitySelectedListener() {
            @Override
            public void onCitySelected(UserSettings.CityID cityID) {
                WeatherAppBus.getBus().post( new SearchEngineCitySelectedEvent( cityID ));
                dismiss();
            }
        });
    }

    private void findViews(View view) {
        searchSummary = view.findViewById(R.id.search_result);
        cityList = view.findViewById(R.id.city_search_result);
    }

}