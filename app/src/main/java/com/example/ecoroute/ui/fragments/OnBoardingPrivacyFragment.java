package com.example.ecoroute.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ecoroute.R;
import com.example.ecoroute.core.constants.DataBaseConstant;
import com.example.ecoroute.databinding.FragmentOnBoardingPrivacyBinding;
import com.example.ecoroute.ui.activity.BottomNavMenu;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;

public class OnBoardingPrivacyFragment extends Fragment {

    private FragmentOnBoardingPrivacyBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<IntentSenderRequest> settingsLauncher;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long animationStartTime;
    private static final long MIN_ANIMATION_TIME = 800, LOCATION_TIMEOUT = 20000;
    private Runnable timeoutRunnable;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setMinUpdateIntervalMillis(500).build();

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION)) || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION))) checkLocationSettings();
            else finishLocationAnimation(() -> Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show());
        });

        settingsLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK) startLocationUpdates();
            else finishLocationAnimation(() -> Toast.makeText(requireContext(), "Please enable location", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnBoardingPrivacyBinding.inflate(inflater, container, false);
        initializeBlur();

        sharedPreferences = requireActivity().getSharedPreferences(DataBaseConstant.DATABASE_NAME, Context.MODE_PRIVATE);

        binding.btnAllowLocation.setOnClickListener(v -> startLocationFlow());

        Fragment parent = getParentFragment();
        if (parent instanceof OnBoardingFragment2) {
            ((OnBoardingFragment2) parent).changeSkipTextVisibility();
        }
        return binding.getRoot();
    }

    private void initializeBlur() {
        binding.blurViewPrivacyFragment.setupWith(binding.blurTarget).setBlurRadius(8f).setBlurAutoUpdate(true);
        binding.blurViewPrivacyFragment.setOverlayColor(ContextCompat.getColor(requireContext(), R.color.blur_overlay));    }

    private void startLocationFlow() {
        showLocationAnimation();
        if (hasLocationPermission()) checkLocationSettings();
        else permissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void checkLocationSettings() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true).build();
        LocationServices.getSettingsClient(requireActivity()).checkLocationSettings(request)
                .addOnSuccessListener(response -> startLocationUpdates())
                .addOnFailureListener(e -> {
                    if (e instanceof ResolvableApiException) {
                        try {
                            settingsLauncher.launch(new IntentSenderRequest.Builder(((ResolvableApiException) e).getResolution()).build());
                        } catch (Exception ex) {
                            finishLocationAnimation(() -> Toast.makeText(requireContext(), "Unable to open location settings", Toast.LENGTH_SHORT).show());
                        }
                    } else finishLocationAnimation(() -> Toast.makeText(requireContext(), "Location settings unavailable", Toast.LENGTH_SHORT).show());
                });
    }

    private void startLocationUpdates() {
        if (!hasLocationPermission()) { finishLocationAnimation(null); return; }

        timeoutRunnable = () -> finishLocationAnimation(() -> Toast.makeText(requireContext(), "Location request timed out", Toast.LENGTH_SHORT).show());
        handler.postDelayed(timeoutRunnable, LOCATION_TIMEOUT);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location location = result == null ? null : result.getLastLocation();
                if (location == null) return;
                stopLocationUpdates();
                finishLocationAnimation(() -> {

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(DataBaseConstant.CURRENT_POSITION_LATITUDE, Double.doubleToRawLongBits(location.getLatitude()));
                    editor.putLong(DataBaseConstant.CURRENT_POSITION_LONGITUDE, Double.doubleToRawLongBits(location.getLongitude()));
                    editor.apply();

                    Intent intent = new Intent(requireActivity(), BottomNavMenu.class);
                    intent.putExtra("latitude", location.getLatitude());
                    intent.putExtra("longitude", location.getLongitude());
                    startActivity(intent);
                    requireActivity().finish();
                });
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) { finishLocationAnimation(null); }
    }

    private void showLocationAnimation() {
        if (binding == null) return;
        animationStartTime = System.currentTimeMillis();
        binding.blurViewPrivacyFragment.setVisibility(View.VISIBLE);
        binding.loaderPrivacyFragment.setVisibility(View.VISIBLE);
        changeParentLogoColor(true);
        binding.blurViewPrivacyFragment.bringToFront();
        binding.loaderPrivacyFragment.bringToFront();
        binding.loaderPrivacyFragment.playAnimation();
    }

    private void finishLocationAnimation(Runnable action) {
        long delay = Math.max(0, MIN_ANIMATION_TIME - (System.currentTimeMillis() - animationStartTime));
        handler.postDelayed(() -> {
            if (binding != null) {
                binding.loaderPrivacyFragment.cancelAnimation();
                binding.loaderPrivacyFragment.setVisibility(View.GONE);
                binding.blurViewPrivacyFragment.setVisibility(View.GONE);
            }
            changeParentLogoColor(false);
            if (action != null && isAdded()) action.run();
        }, delay);
    }

    private void stopLocationUpdates() {
        if (timeoutRunnable != null) { handler.removeCallbacks(timeoutRunnable); timeoutRunnable = null; }
        if (locationCallback != null) { fusedLocationClient.removeLocationUpdates(locationCallback); locationCallback = null; }
    }

    private void changeParentLogoColor(boolean isLoading) {
        if (getParentFragment() instanceof OnBoardingFragment2) {
            ((OnBoardingFragment2) getParentFragment()).changeLogoContainerColor(isLoading);
        }
    }

    @Override
    public void onDestroyView() {
        stopLocationUpdates();
        handler.removeCallbacksAndMessages(null);
        if (binding != null) binding.loaderPrivacyFragment.cancelAnimation();
        binding = null;
        super.onDestroyView();
    }
}
