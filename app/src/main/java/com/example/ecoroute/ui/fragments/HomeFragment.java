package com.example.ecoroute.ui.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ecoroute.core.api.getAQI.AQIClient;
import com.example.ecoroute.core.api.getAQI.AQIResponseData;
import com.example.ecoroute.databinding.FragmentHomeBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private double latitude , longitude;
    private ExecutorService executor;
    private final String TAG = "AQI_API";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            latitude = args.getDouble("latitude");
            longitude = args.getDouble("longitude");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater , container , false);
        executor = Executors.newCachedThreadPool();

        getAndSetAqi(latitude , longitude);
        getCityName(latitude , longitude);

        return binding.getRoot();
    }

    private void getAndSetAqi(double latitude, double longitude) {
        executor.execute(() -> {
            AQIClient.getAQI(requireContext(), latitude, longitude, new AQIClient.AQICallback() {
                @Override
                public void onSuccess(AQIResponseData response) {
                    AQIResponseData.CurrentData current = response.getCurrent();
                    String currentDateTime = new SimpleDateFormat("dd MMMM yyyy hh:mm a", Locale.getDefault()).format(new Date());

                    binding.aqiTextview.setText(String.valueOf(current.getUsAqi()));
                    binding.aqiStatusTextview.setText("Moderate");
                    binding.timeTextview.setText(currentDateTime);

                    binding.PM25DataTextview.setText(String.valueOf(current.getPm25()));
                    binding.PM10DataTextview.setText(String.valueOf(current.getPm10()));
                    binding.NO2DataTextview.setText(String.valueOf(current.getNitrogenDioxide()));
                    binding.O3DataTextview.setText(String.valueOf(current.getOzone()));
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG , error);
                }
            });
        });
    }

    private void getCityName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

        if (!Geocoder.isPresent()) {
            Log.e(TAG, "Geocoder is not available");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1, addresses -> {
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String city = address.getLocality();
                    String state = address.getAdminArea();
                    String location = (city != null ? city : "") + (state != null ? ", " + state : "");

                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.currentCityStateTextview.setText(location);
                    });
                } else {
                    Log.e(TAG, "No address found for coordinates: " + latitude + ", " + longitude);
                }
            });
        } else {
            executor.execute(() -> {
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String city = address.getLocality();
                        String state = address.getAdminArea();
                        String location = (city != null ? city : "") + (state != null ? ", " + state : "");

                        requireActivity().runOnUiThread(() -> {
                            if (binding != null) binding.currentCityStateTextview.setText(location);
                        });
                    } else {
                        Log.e(TAG, "No address found for coordinates: " + latitude + ", " + longitude);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Geocoder error", e);
                }
            });
        }
    }

}