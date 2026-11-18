package com.example.ecoroute.ui.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ecoroute.R;
import com.example.ecoroute.core.constants.DataBaseConstant;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapFragment extends Fragment {

    private static final String TAG = "OSM_TEST";
    private SharedPreferences sharedPreferences;
    private MapView mapView;
    private double latitude, longitude;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private ImageButton currentLocationButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializePreferences();
        initializeMap(view);
        initializeLocationServices();
        ImageButton currentLocationButton = view.findViewById(R.id.btnCurrentLocation);
        currentLocationButton.setOnClickListener(v -> getCurrentLocation());
    }

    private void initializePreferences() {
        sharedPreferences = requireContext().getSharedPreferences(DataBaseConstant.DATABASE_NAME, Context.MODE_PRIVATE);
    }

    private void initializeMap(View view) {
        Configuration.getInstance().setUserAgentValue("EcoRoute/1.0 (Android; com.example.ecoroute)");
        Configuration.getInstance().setDebugMode(false);
        Configuration.getInstance().setDebugMapView(false);

        mapView = view.findViewById(R.id.osmMap);

        if (mapView == null) {
            Log.e(TAG, "MapView not found in XML");
            return;
        }

        configureMap();
        configureTileSource();
        loadCurrentLocation();
        setMapPosition();
        addCurrentLocationMarker();

        Log.d(TAG, "OSMDroid Fragment initialized successfully");
    }

    private void configureMap() {
        mapView.setUseDataConnection(true);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        mapView.setMaxZoomLevel(19.0);
    }

    private void configureTileSource() {
        mapView.setTileSource(new XYTileSource("OpenStreetMap", 0, 19, 256, ".png", new String[]{"https://tile.openstreetmap.org/"}));
    }

    private void loadCurrentLocation() {
        latitude = Double.longBitsToDouble(sharedPreferences.getLong(DataBaseConstant.CURRENT_POSITION_LATITUDE, Double.doubleToRawLongBits(21.1702)));
        longitude = Double.longBitsToDouble(sharedPreferences.getLong(DataBaseConstant.CURRENT_POSITION_LONGITUDE, Double.doubleToRawLongBits(72.8311)));
    }

    private void setMapPosition() {
        GeoPoint currentLocation = new GeoPoint(latitude, longitude);
        mapView.getController().setZoom(16.0);
        mapView.getController().setCenter(currentLocation);
    }

    private void addCurrentLocationMarker() {
        GeoPoint currentLocation = new GeoPoint(latitude, longitude);
        Marker marker = new Marker(mapView);
        marker.setPosition(currentLocation);
        marker.setTitle("Your Current Location");
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
    }

    private void initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    Boolean fine = permissions.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarse = permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                    if (Boolean.TRUE.equals(fine) || Boolean.TRUE.equals(coarse)) {
                        getCurrentLocation();
                    }
                }
        );
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
            return;
        }

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null && mapView != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        GeoPoint currentLocation = new GeoPoint(latitude, longitude);
                        mapView.getController().animateTo(currentLocation);
                        mapView.getController().setZoom(17.0);

                        sharedPreferences.edit()
                                .putLong(DataBaseConstant.CURRENT_POSITION_LATITUDE, Double.doubleToRawLongBits(latitude))
                                .putLong(DataBaseConstant.CURRENT_POSITION_LONGITUDE, Double.doubleToRawLongBits(longitude))
                                .apply();

                        mapView.invalidate();
                    } else {
                        Log.e("LOCATION", "Unable to get current location");
                    }
                })
                .addOnFailureListener(e -> Log.e("LOCATION", "Location error: " + e.getMessage()));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) {
            mapView.onDetach();
            mapView = null;
        }
        super.onDestroyView();
    }
}