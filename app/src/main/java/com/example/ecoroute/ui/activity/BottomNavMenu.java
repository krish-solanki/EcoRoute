
package com.example.ecoroute.ui.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecoroute.R;
import com.example.ecoroute.core.helper.ScreenArrangement;
import com.example.ecoroute.databinding.ActivityBottomNavMenuBinding;
import com.example.ecoroute.ui.adapter.BottomNavigationAdapter;

public class BottomNavMenu extends AppCompatActivity {

    private ActivityBottomNavMenuBinding binding;
    private BottomNavigationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        double latitude = getIntent().getDoubleExtra("latitude", 0.0);
        double longitude = getIntent().getDoubleExtra("longitude", 0.0);

        binding = ActivityBottomNavMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new BottomNavigationAdapter(this, latitude, longitude);
        binding.viewPagerBottomNavView.setAdapter(adapter);
        binding.viewPagerBottomNavView.setUserInputEnabled(false);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home_menu) {
                binding.viewPagerBottomNavView.setCurrentItem(0, false);
                return true;
            } else if (id == R.id.map_menu) {
                binding.viewPagerBottomNavView.setCurrentItem(1, false);
                return true;
            } else if (id == R.id.history_menu) {
                binding.viewPagerBottomNavView.setCurrentItem(2, false);
                return true;
            } else if (id == R.id.profile_menu) {
                binding.viewPagerBottomNavView.setCurrentItem(3, false);
                return true;
            }

            return false;
        });

        binding.viewPagerBottomNavView.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                int menuId;

                switch (position) {
                    case 0: menuId = R.id.home_menu; break;
                    case 1: menuId = R.id.map_menu; break;
                    case 2: menuId = R.id.history_menu; break;
                    case 3: menuId = R.id.profile_menu; break;
                    default: menuId = R.id.home_menu; break;
                }

                binding.bottomNavigationView.setSelectedItemId(menuId);
            }
        });
    }
}
