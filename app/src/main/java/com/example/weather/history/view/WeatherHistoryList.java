package com.example.weather.history.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.weather.R;
import com.example.weather.WeatherApp;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WeatherHistoryList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeatherHistoryList extends Fragment {

    private RecyclerView historyList;
    private HistorySource historySource;
    private HistoryListAdapter historyListAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public WeatherHistoryList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WeatherHistoryList.
     */
    // TODO: Rename and change types and number of parameters
    public static WeatherHistoryList newInstance() {
        WeatherHistoryList fragment = new WeatherHistoryList();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather_history_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true);
        findView(view);
        setupHistoryList();
    }

    private void setupHistoryList() {
        historyList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        historyList.setLayoutManager(layoutManager);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(requireContext(),  LinearLayoutManager.VERTICAL);
        itemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(), R.drawable.recycleview_separator)));
        historyList.addItemDecoration(itemDecoration);

        historySource = new HistorySource(
                WeatherApp.getInstance().getWeatherCityDAO(),
                WeatherApp.getInstance().getWeatherIconsDAO(),
                WeatherApp.getInstance().getWeatherHistoryDAO()
        );


        new Thread(new Runnable() {
            @Override
            public void run() {
                historySource.reloadHistoryFromDB(null);

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        historyListAdapter = new HistoryListAdapter(requireContext(), historySource);
                        historyList.setAdapter(historyListAdapter);
                    }
                });
            }
        }).start();
    }

    private void findView(View view) {
        historyList = view.findViewById(R.id.history_list);
    }
}