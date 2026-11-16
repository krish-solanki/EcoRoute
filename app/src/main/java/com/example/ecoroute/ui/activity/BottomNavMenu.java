package com.example.ecoroute.ui.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        binding = ActivityBottomNavMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ScreenArrangement.adjustStatusBarHeight(binding.statusBarBottomNavBarScreen);

        adapter = new BottomNavigationAdapter(this);
        binding.viewPagerBottomNavView.setAdapter(adapter);
        binding.viewPagerBottomNavView.setUserInputEnabled(false);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home_menu){
                return  true;
            } else if (id == R.id.map_menu) {
                return true;
            } else if (id == R.id.history_menu) {
                return true;
            }else if (id == R.id.profile_menu){
                return true;
            }else {
                return false;
            }
        });
    }
}