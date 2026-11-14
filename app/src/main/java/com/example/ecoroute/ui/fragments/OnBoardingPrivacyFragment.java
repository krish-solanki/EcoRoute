package com.example.ecoroute.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ecoroute.R;
import com.example.ecoroute.databinding.FragmentOnBoardingPrivacyBinding;

public class OnBoardingPrivacyFragment extends Fragment {
    private FragmentOnBoardingPrivacyBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnBoardingPrivacyBinding.inflate(inflater, container, false);
        Fragment parent = getParentFragment();

        if (parent instanceof OnBoardingFragment2) {
            ((OnBoardingFragment2) parent).changeSkipTextVisibility();
        }
        return binding.getRoot();
    }
}