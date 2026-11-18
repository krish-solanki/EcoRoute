package com.example.ecoroute.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecoroute.R;
import com.example.ecoroute.core.helper.ScreenArrangement;
import com.example.ecoroute.model.history.RouteHistoryModel;
import com.example.ecoroute.ui.adapter.RouteHistoryAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private View rootView , statusBarHeight;
    private RecyclerView historyRecyclerView;
    private EditText historySearchEditText;
    private TextView historyFilterAll, historyFilterSearched, historyFilterStarted;
    private RouteHistoryAdapter adapter;
    private List<RouteHistoryModel> allRoutes = new ArrayList<>();
    private String selectedFilter = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_history, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        statusBarHeight = view.findViewById(R.id.viewStatusBarHeightHistoryFragment);
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView);
        historySearchEditText = view.findViewById(R.id.historySearchEditText);
        historyFilterAll = view.findViewById(R.id.historyFilterAll);
        historyFilterSearched = view.findViewById(R.id.historyFilterSearched);
        historyFilterStarted = view.findViewById(R.id.historyFilterStarted);

        ScreenArrangement.adjustStatusBarHeight(statusBarHeight);
        setupRecyclerView();
        loadSampleHistory();
        setupSearch();
        setupFilters();
    }

    private void setupRecyclerView() {
        adapter = new RouteHistoryAdapter(new ArrayList<>(), item -> {
            // TODO: Open route details or restore route on card click.
        });

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyRecyclerView.setAdapter(adapter);
        historyRecyclerView.setHasFixedSize(false);
    }

    private void loadSampleHistory() {
        allRoutes.clear();

        allRoutes.add(new RouteHistoryModel("Satellite, Ahmedabad", "Navrangpura, Ahmedabad", "Started", "Today · 9:41 AM", "6.4 km"));
        allRoutes.add(new RouteHistoryModel("Prahlad Nagar, Ahmedabad", "Riverfront Park, Ahmedabad", "Searched", "Today · 8:12 AM", "4.8 km"));
        allRoutes.add(new RouteHistoryModel("Vastrapur Lake, Ahmedabad", "IIM Ahmedabad", "Started", "Yesterday · 6:25 PM", "3.2 km"));
        allRoutes.add(new RouteHistoryModel("Maninagar, Ahmedabad", "Kankaria Lake, Ahmedabad", "Searched", "Yesterday · 5:40 PM", "2.1 km"));

        applyFilters();
    }

    private void setupSearch() {
        historySearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        historyFilterAll.setOnClickListener(v -> selectFilter("All"));
        historyFilterSearched.setOnClickListener(v -> selectFilter("Searched"));
        historyFilterStarted.setOnClickListener(v -> selectFilter("Started"));
    }

    private void selectFilter(String filter) {
        selectedFilter = filter;

        historyFilterAll.setBackgroundResource(filter.equals("All") ? R.drawable.history_filter_selected : R.drawable.history_filter_unselected);
        historyFilterSearched.setBackgroundResource(filter.equals("Searched") ? R.drawable.history_filter_selected : R.drawable.history_filter_unselected);
        historyFilterStarted.setBackgroundResource(filter.equals("Started") ? R.drawable.history_filter_selected : R.drawable.history_filter_unselected);

        historyFilterAll.setTextColor(getResources().getColor(filter.equals("All") ? R.color.white : R.color.text_primary));
        historyFilterSearched.setTextColor(getResources().getColor(filter.equals("Searched") ? R.color.white : R.color.text_primary));
        historyFilterStarted.setTextColor(getResources().getColor(filter.equals("Started") ? R.color.white : R.color.text_primary));

        applyFilters();
    }

    private void applyFilters() {
        if (adapter == null || historySearchEditText == null) return;

        String query = historySearchEditText.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<RouteHistoryModel> filtered = new ArrayList<>();

        for (RouteHistoryModel item : allRoutes) {
            boolean matchesFilter = selectedFilter.equals("All") || item.getStatus().equalsIgnoreCase(selectedFilter);
            boolean matchesSearch = item.getStartLocation().toLowerCase(Locale.ROOT).contains(query) || item.getDestination().toLowerCase(Locale.ROOT).contains(query);

            if (matchesFilter && matchesSearch) filtered.add(item);
        }

        adapter.updateList(filtered);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        historyRecyclerView = null;
        historySearchEditText = null;
        historyFilterAll = null;
        historyFilterSearched = null;
        historyFilterStarted = null;
        rootView = null;
    }
}