package com.example.ecoroute.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ecoroute.R;
import com.example.ecoroute.databinding.FragmentOnBoardingPrivacyBinding;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;

public class OnBoardingPrivacyFragment extends Fragment {

    private FragmentOnBoardingPrivacyBinding binding;

    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<IntentSenderRequest> gpsLauncher;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    if (isPermissionGranted()) {
                        checkGPS();
                    } else {
                        Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        gpsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        getFreshLocation();
                    } else {
                        Toast.makeText(requireContext(),"Please enable GPS to continue", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnBoardingPrivacyBinding.inflate(inflater, container, false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        Fragment parent = getParentFragment();
        if (parent instanceof OnBoardingFragment2) {
            ((OnBoardingFragment2) parent).changeSkipTextVisibility();
        }

        binding.btnAllowLocation.setOnClickListener(v -> requestLocationPermission());
        return binding.getRoot();
    }

    private void requestLocationPermission() {

        if (isPermissionGranted()) {
            checkGPS();
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void checkGPS() {
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();

        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                        .addLocationRequest(request)
                        .setAlwaysShow(true)
                        .build();

        LocationServices.getSettingsClient(requireActivity())
                .checkLocationSettings(settingsRequest)
                .addOnSuccessListener(response -> getFreshLocation())
                .addOnFailureListener(exception -> {

                    if (exception instanceof ResolvableApiException) {
                        try {
                            IntentSenderRequest intentRequest = new IntentSenderRequest.Builder(
                                            ((ResolvableApiException) exception).getResolution())
                                            .build();

                            gpsLauncher.launch(intentRequest);

                        } catch (Exception e) {
                            Log.e("LocationDebug", "GPS dialog error", e);
                        }
                    } else {
                        Toast.makeText(requireContext(), "Unable to check GPS settings", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void getFreshLocation() {
        if (!isPermissionGranted()) return;

        Toast.makeText(requireContext() , "Fetching your location...", Toast.LENGTH_SHORT).show();

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(500)
                .setMaxUpdates(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location location = result.getLastLocation();
                if (location != null) {
                    showCoordinates(location);
                } else {
                    Toast.makeText(requireContext(), "Location not available. Try again.", Toast.LENGTH_LONG).show();
                }

                fusedLocationProviderClient.removeLocationUpdates(this);
                locationCallback = null;
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
                .addOnFailureListener(e -> {
                    Log.e("LocationDebug", "Location request failed", e);

                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Location error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showCoordinates(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Toast.makeText(requireContext(), "Latitude: " + latitude + "\nLongitude: " + longitude, Toast.LENGTH_LONG).show();

        Log.d("LocationCoordinates", "Latitude: " + latitude + ", Longitude: " + longitude);

        Log.d("LocationDebug", "Accuracy: " + location.getAccuracy() + " metres");
    }

    @Override
    public void onDestroyView() {

        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }

        binding = null;
        super.onDestroyView();
    }
}
