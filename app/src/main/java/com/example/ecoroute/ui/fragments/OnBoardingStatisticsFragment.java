package com.example.ecoroute.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ecoroute.R;
import com.example.ecoroute.databinding.FragmentOnBoardingStatisticsBinding;

public class OnBoardingStatisticsFragment extends Fragment {
    private FragmentOnBoardingStatisticsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnBoardingStatisticsBinding.inflate(inflater, container, false);
        binding.statisticsFragmentContinueButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.onBoardingContainer, new OnBoardingPrivacyFragment())
                    .addToBackStack(null)
                    .commit();

        });
        binding.statisticsFragmentBackButton.setOnClickListener(v -> {});
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}