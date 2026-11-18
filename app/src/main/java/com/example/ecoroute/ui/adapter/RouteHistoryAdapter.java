package com.example.ecoroute.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecoroute.R;
import com.example.ecoroute.databinding.RouteHistoryLayoutBinding;
import com.example.ecoroute.model.history.RouteHistoryModel;

import java.util.List;

public class RouteHistoryAdapter extends RecyclerView.Adapter<RouteHistoryAdapter.RouteViewHolder> {

    public interface OnRouteClickListener {
        void onRouteClick(RouteHistoryModel item);
    }

    private List<RouteHistoryModel> routeList;
    private final OnRouteClickListener listener;

    public RouteHistoryAdapter(List<RouteHistoryModel> routeList, OnRouteClickListener listener) {
        this.routeList = routeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RouteHistoryLayoutBinding binding = RouteHistoryLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RouteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        RouteHistoryModel item = routeList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public void updateList(List<RouteHistoryModel> newList) {
        routeList = newList;
        notifyDataSetChanged();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        private final RouteHistoryLayoutBinding binding;

        RouteViewHolder(RouteHistoryLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RouteHistoryModel item, OnRouteClickListener listener) {
            binding.historyStartLocation.setText(item.getStartLocation());
            binding.historyDestinationLocation.setText(item.getDestination());
            binding.historyRouteStatus.setText(item.getStatus());
            binding.historyRouteDate.setText(item.getDate());
            binding.historyRouteDistance.setText(item.getDistance());

            if (item.getStatus().equalsIgnoreCase("Started")) {
                binding.historyRouteStatus.setBackgroundResource(R.drawable.status_started_bg);
            } else {
                binding.historyRouteStatus.setBackgroundResource(R.drawable.status_end_bg);
            }

            binding.getRoot().setOnClickListener(v -> listener.onRouteClick(item));
        }
    }
}