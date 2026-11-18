package com.example.ecoroute.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecoroute.R;
import com.example.ecoroute.core.constants.DataBaseConstant;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class DemoMapAct extends AppCompatActivity {

    private static final String TAG = "OSM_TEST";

    private SharedPreferences sharedPreferences;
    private MapView mapView;

    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_map);

        initializePreferences();
        initializeMap();
    }

    // Initialize SharedPreferences
    private void initializePreferences() {
        sharedPreferences = getSharedPreferences(DataBaseConstant.DATABASE_NAME, MODE_PRIVATE);
    }

    // Initialize OSMDroid
    private void initializeMap() {

        Configuration.getInstance().setUserAgentValue("EcoRoute/1.0 (Android; com.example.ecoroute)");
        Configuration.getInstance().setDebugMode(false);
        Configuration.getInstance().setDebugMapView(false);

        mapView = findViewById(R.id.osmMap);
        if (mapView == null) {
            Log.e(TAG, "MapView not found in XML");
            return;
        }

        configureMap();
        configureTileSource();
        loadCurrentLocation();
        setMapPosition();
        addCurrentLocationMarker();

        Log.d(TAG, "OSMDroid initialized successfully");
    }

    // Configure map controls
    private void configureMap() {

        mapView.setUseDataConnection(true);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);

        mapView.setMinZoomLevel(6.0);
    }

    // Configure OpenStreetMap tile source
    private void configureTileSource() {
        mapView.setTileSource(
                new XYTileSource(
                        "OpenStreetMap-English",
                        0,
                        19,
                        256,
                        ".png",
                        new String[]{"https://tile.openstreetmap.org/"})
        );
    }

    // Get saved current location
    private void loadCurrentLocation() {
        latitude = Double.longBitsToDouble(sharedPreferences.getLong(DataBaseConstant.CURRENT_POSITION_LATITUDE, Double.doubleToRawLongBits(21.1702)));
        longitude = Double.longBitsToDouble(sharedPreferences.getLong(DataBaseConstant.CURRENT_POSITION_LONGITUDE, Double.doubleToRawLongBits(72.8311)));
    }

    // Set camera position
    private void setMapPosition() {
        GeoPoint currentLocation = new GeoPoint(latitude, longitude);
        mapView.getController().setZoom(16.0);
        mapView.getController().setCenter(currentLocation);
    }

    // Add current location marker
    private void addCurrentLocationMarker() {
        GeoPoint currentLocation = new GeoPoint(latitude, longitude);
        Marker marker = new Marker(mapView);
        marker.setPosition(currentLocation);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
    }

    // Activity lifecycle
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) {
            mapView.onDetach();
        }
        super.onDestroy();
    }
}
