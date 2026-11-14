package com.example.ecoroute.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ecoroute.R;
import com.example.ecoroute.databinding.FragmentOnBoarding1Binding; // Generated from fragment_on_boarding1.xml

public class OnBoardingFragment1 extends Fragment {
    private FragmentOnBoarding1Binding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnBoarding1Binding.inflate(inflater, container, false);
        binding.onBoardingFragmentGetStartedBtn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.onBoardingScreenFragmentContainer, new OnBoardingFragment2())
                    .commit();
        });
        return binding.getRoot();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                    }
                }
        );
    }
}