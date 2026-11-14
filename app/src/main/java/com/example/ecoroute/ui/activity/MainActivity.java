package com.example.ecoroute.ui.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecoroute.core.helper.ScreenArrangement;
import com.example.ecoroute.databinding.ActivityMainBinding;
import com.example.ecoroute.ui.fragments.OnBoardingFragment1;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

//        ScreenArrangement.adjustNavigationBarHeight(binding.viewForStatusBarMainActivity);
        ScreenArrangement.adjustNavigationBarHeight(binding.viewForNavigationBarMainActivity);
        getSupportFragmentManager().beginTransaction()
                .replace(binding.onBoardingScreenFragmentContainer.getId(), new OnBoardingFragment1())
                .commit();
    }
}