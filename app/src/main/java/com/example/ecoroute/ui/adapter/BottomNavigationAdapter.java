
package com.example.ecoroute.ui.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.ecoroute.ui.fragments.HistoryFragment;
import com.example.ecoroute.ui.fragments.HomeFragment;
import com.example.ecoroute.ui.fragments.MapFragment;

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

                HomeFragment homeFragment = new HomeFragment();
                homeFragment.setArguments(bundle);
                return homeFragment;

            case 1:
                return new MapFragment();

            case 2:
                return new HistoryFragment();

            case 3:
                return new MapFragment();

            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
