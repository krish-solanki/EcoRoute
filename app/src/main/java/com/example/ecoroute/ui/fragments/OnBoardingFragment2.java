package com.example.ecoroute.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ecoroute.R;
import com.example.ecoroute.core.helper.ScreenArrangement;
import com.example.ecoroute.databinding.FragmentOnBoarding1Binding;
import com.example.ecoroute.databinding.FragmentOnBoarding2Binding;

public class OnBoardingFragment2 extends Fragment {
    private FragmentOnBoarding2Binding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnBoarding2Binding.inflate(inflater, container, false);
        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.onBoardingContainer, new OnBoardingStatisticsFragment())
                    .commit();

            binding.onBoardingStatisticsAndPrivacyContainer.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.screen_2_bg)
            );
        }

        return binding.getRoot();
    }

    public void changeSkipTextVisibility() {
        if (binding != null) {
            if (binding.tvSkip.getVisibility() == View.VISIBLE) {
                binding.tvSkip.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Do nothing - disable back navigation
                    }
                }
        );
    }
}