package com.example.ecoroute.ui.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.ecoroute.R;
import com.example.ecoroute.core.api.getAQI.AQIClient;
import com.example.ecoroute.core.api.getAQI.AQIResponseData;
import com.example.ecoroute.core.constants.IndiaCities;
import com.example.ecoroute.core.helper.ScreenArrangement;
import com.example.ecoroute.databinding.FragmentHomeBinding;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private double latitude , longitude;
    private ExecutorService executor;
    private final String TAG = "AQI_API";
    private FusedLocationProviderClient fusedLocationClient;

    private ShimmerFrameLayout skeleton;
    private View skeletonContainer;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater , container , false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        executor = Executors.newCachedThreadPool();
        ScreenArrangement.adjustStatusBarHeight(binding.viewStatusBarHomeFragment);
        skeletonContainer = binding.getRoot().findViewById(R.id.cardViewAqiSkeletonHomeFragment);
        skeleton = (ShimmerFrameLayout) skeletonContainer;

        if (longitude != 0 && latitude != 0){
            getAndSetAqi(latitude , longitude);
        }else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                setCurrentLocation();
            }
            else {
                IndiaCities.City defaultCity = IndiaCities.ALL_CITIES.get(0);
                latitude = defaultCity.latitude;
                longitude = defaultCity.longitude;
                binding.currentCityStateTextview.setText(defaultCity.toString());
                getAndSetAqi(latitude, longitude);
            }
        }

        getCityName(latitude , longitude);

        binding.locationSelector.setOnClickListener(v -> showCityDialog());
        refreshLayout();

        return binding.getRoot();
    }

    private void refreshLayout() {
        binding.swipeRefreshLayoutHomeFragment.setOnRefreshListener(() -> {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                setCurrentLocation();
            }
            else {
                latitude = 0.0;
                longitude = 0.0;
                getAndSetAqi(latitude, longitude);
                binding.swipeRefreshLayoutHomeFragment.setRefreshing(false);
            }
        });
    }

    private void setCurrentLocation(){
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (binding == null) return;

                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else {
                    latitude = 0.0;
                    longitude = 0.0;
                }

                getAndSetAqi(latitude, longitude);
                getCityName(latitude , longitude);
                binding.swipeRefreshLayoutHomeFragment.setRefreshing(false);
            }).addOnFailureListener(e -> {
                latitude = 0.0;
                longitude = 0.0;
                getAndSetAqi(latitude, longitude);
                if (binding != null)
                    binding.swipeRefreshLayoutHomeFragment.setRefreshing(false);
            });

        }
    }

    private void getAndSetAqi(double latitude, double longitude) {
        executor.execute(() -> {
            requireActivity().runOnUiThread(this::showAqiSkeleton);
            AQIClient.getAQI(getContext(), latitude, longitude, new AQIClient.AQICallback() {
                @Override
                public void onSuccess(AQIResponseData response) {
                    AQIResponseData.CurrentData current = response.getCurrent();
                    String currentDateTime = new SimpleDateFormat("dd MMMM yyyy hh:mm a", Locale.getDefault()).format(new Date());

                    hideAqiSkeleton();

                    binding.aqiTextview.setText(String.valueOf(current.getUsAqi()));
                    if (current.getUsAqi() <=50){
                        binding.aqiTextview.setTextColor(ContextCompat.getColor(requireContext(), R.color.aqi_category_good));
                    }else if (current.getUsAqi()>50 && current.getUsAqi() <= 100){
                        binding.aqiTextview.setTextColor(ContextCompat.getColor(requireContext(), R.color.aqi_category_satisfactory));
                    }else if (current.getUsAqi()>100 && current.getUsAqi() <= 200){
                        binding.aqiTextview.setTextColor(ContextCompat.getColor(requireContext(), R.color.aqi_category_moderate));
                    }else if (current.getUsAqi()>200 && current.getUsAqi() <= 300){
                        binding.aqiTextview.setTextColor(ContextCompat.getColor(requireContext(), R.color.aqi_category_poor));
                    }else if (current.getUsAqi()>300 && current.getUsAqi() <= 400){
                        binding.aqiTextview.setTextColor(ContextCompat.getColor(requireContext(), R.color.aqi_category_very_poor));
                    }else if (current.getUsAqi()>400){
                        binding.aqiTextview.setTextColor(ContextCompat.getColor(requireContext(), R.color.aqi_category_severe));
                    }

                    if (current.getUsAqi() <=50){
                        binding.aqiStatusTextview.setText("Good");
                        binding.bgStatusTextview.setBackgroundResource(R.drawable.bg_aqi_good);
                    }else if (current.getUsAqi()>50 && current.getUsAqi() <= 100){
                        binding.aqiStatusTextview.setText("Satisfactory");
                        binding.bgStatusTextview.setBackgroundResource(R.drawable.bg_aqi_satisfactory);
                    }else if (current.getUsAqi()>100 && current.getUsAqi() <= 200){
                        binding.aqiStatusTextview.setText("Moderate");
                        binding.bgStatusTextview.setBackgroundResource(R.drawable.bg_aqi_moderate);
                    }else if (current.getUsAqi()>200 && current.getUsAqi() <= 300){
                        binding.aqiStatusTextview.setText("Poor ");
                        binding.bgStatusTextview.setBackgroundResource(R.drawable.bg_aqi_poor);
                    }else if (current.getUsAqi()>300 && current.getUsAqi() <= 400){
                        binding.aqiStatusTextview.setText("Very Poor");
                        binding.bgStatusTextview.setBackgroundResource(R.drawable.bg_aqi_very_poor);
                    }else if (current.getUsAqi()>400){
                        binding.aqiStatusTextview.setText("Severe");
                        binding.bgStatusTextview.setBackgroundResource(R.drawable.bg_aqi_severe);
                    }


                    binding.timeTextview.setText(currentDateTime);

                    binding.PM25DataTextview.setText(String.valueOf(current.getPm25()));
                    binding.PM10DataTextview.setText(String.valueOf(current.getPm10()));
                    binding.NO2DataTextview.setText(String.valueOf(current.getNitrogenDioxide()));
                    binding.O3DataTextview.setText(String.valueOf(current.getOzone()));
                }

                @Override
                public void onError(String error) {
                    hideAqiSkeleton();
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

    private void showCityDialog() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.select_city_dialog, null);
        EditText search = view.findViewById(R.id.search_city);
        ListView listView = view.findViewById(R.id.city_list);

        ArrayAdapter<IndiaCities.City> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>(IndiaCities.ALL_CITIES));
        listView.setAdapter(adapter);
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(view).create();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            IndiaCities.City selectedCity = adapter.getItem(position);
            if (selectedCity == null) return;

            latitude = selectedCity.latitude;
            longitude = selectedCity.longitude;

            binding.currentCityStateTextview.setText(selectedCity.toString());
            getAndSetAqi(latitude, longitude);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void showAqiSkeleton() {
        binding.cardViewAqiHomeFragment.setVisibility(View.GONE);
        skeletonContainer.setVisibility(View.VISIBLE);
        skeleton.startShimmer();
    }

    private void hideAqiSkeleton() {
        skeleton.stopShimmer();
        skeletonContainer.setVisibility(View.GONE);
        binding.cardViewAqiHomeFragment.setVisibility(View.VISIBLE);
    }
}