package com.example.ecoroute.ui.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.ecoroute.ui.fragments.HomeFragment;

public class BottomNavigationAdapter extends FragmentStateAdapter {
    private final double latitude, longitude;

    public BottomNavigationAdapter(FragmentActivity activity, double latitude, double longitude) {
        super(activity);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                Bundle bundle = new Bundle();
                bundle.putDouble("latitude", latitude);
                bundle.putDouble("longitude", longitude);

                HomeFragment fragment = new HomeFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 1:
                return new HomeFragment();
            case 2:
                return new HomeFragment();
            case 3:
                return new HomeFragment();
            default:
                return new HomeFragment();
        }    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
