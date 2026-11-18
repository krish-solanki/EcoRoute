package com.example.ecoroute.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ecoroute.R;
import com.example.ecoroute.core.constants.DataBaseConstant;
import com.example.ecoroute.core.helper.ScreenArrangement;
import com.example.ecoroute.databinding.FragmentMapBinding;
import com.example.ecoroute.databinding.UserLocationSearchDialogBinding;
import com.example.ecoroute.model.map.LocationSuggestion;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.osmdroid.views.overlay.Polyline;
import java.util.Locale;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.Marker;
import org.json.JSONObject;
import org.json.JSONArray;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapFragment extends Fragment {

    private static final String TAG = "OSM_TEST";

    private FragmentMapBinding binding;
    private UserLocationSearchDialogBinding searchBinding;
    private SharedPreferences sharedPreferences;
    private MapView mapView;
    private double latitude, longitude;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private Marker startMarker;
    private Marker destinationMarker;
    private Polyline routePolyline;
    private BottomSheetDialog searchBottomSheet;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private ExecutorService searchExecutor = Executors.newSingleThreadExecutor();
    private Runnable searchRunnable;
    private int searchRequestId = 0;
    private LocationSuggestion selectedStartLocation;
    private LocationSuggestion selectedDestination;
    private boolean searchingStartLocation = true;
    private boolean isViewActive = false;
    private final ExecutorService routingExecutor = Executors.newSingleThreadExecutor();
    private SensorManager sensorManager;
    private Sensor rotationSensor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewActive = true;
        initializePreferences();
        initializeMap();
        initializeCompass();
        initializeLocationServices();
        ScreenArrangement.adjustStatusBarHeight(binding.viewStatusBarMapFragment);
        binding.btnCurrentLocation.setOnClickListener(v -> openLocationSearch());
    }

    private void initializePreferences() {
        sharedPreferences = requireContext().getSharedPreferences(DataBaseConstant.DATABASE_NAME, Context.MODE_PRIVATE);
    }

    private void initializeMap() {
        Configuration.getInstance().setUserAgentValue("EcoRoute/1.0 (Android; com.example.ecoroute)");
        Configuration.getInstance().setDebugMode(false);
        Configuration.getInstance().setDebugMapView(false);
        mapView = binding.osmMap;
        if (mapView == null) {
            Log.e(TAG, "MapView not found in XML");
            return;
        }
        configureMap();
        configureTileSource();
        loadCurrentLocation();
        setMapPosition();
        addCurrentLocationMarker();
    }

    private void initializeCompass() {
        sensorManager = (SensorManager)
                requireContext().getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            rotationSensor = sensorManager.getDefaultSensor(
                    Sensor.TYPE_ROTATION_VECTOR
            );
        }
    }

    private void configureMap() {
        mapView.setUseDataConnection(true);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        mapView.setMaxZoomLevel(19.0);
        mapView.setMapOrientation(0f);
    }

    private void configureTileSource() {
        mapView.setTileSource(new XYTileSource("OpenStreetMap", 0, 19, 256, ".png", new String[]{"https://tile.openstreetmap.org/"}));
    }

    private void loadCurrentLocation() {
        latitude = Double.longBitsToDouble(sharedPreferences.getLong(DataBaseConstant.CURRENT_POSITION_LATITUDE, Double.doubleToRawLongBits(21.1702)));
        longitude = Double.longBitsToDouble(sharedPreferences.getLong(DataBaseConstant.CURRENT_POSITION_LONGITUDE, Double.doubleToRawLongBits(72.8311)));
    }

    private void setMapPosition() {
        mapView.getController().setZoom(16.0);
        mapView.getController().setCenter(new GeoPoint(latitude, longitude));
    }

    private void addCurrentLocationMarker() {
        if (mapView == null) return;

        if (startMarker != null) {
            mapView.getOverlays().remove(startMarker);
        }

        startMarker = new Marker(mapView);
        startMarker.setPosition(new GeoPoint(latitude, longitude));
        startMarker.setTitle("Current Location");
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(resizeMarkerIcon(R.drawable.current_location, 32));

        mapView.getOverlays().add(startMarker);
        mapView.invalidate();
    }

    private void addStartMarker(double lat, double lon, String title) {
        if (mapView == null) return;

        if (startMarker != null) {
            mapView.getOverlays().remove(startMarker);
        }

        startMarker = new Marker(mapView);
        startMarker.setPosition(new GeoPoint(lat, lon));
        startMarker.setTitle(title);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(resizeMarkerIcon(R.drawable.current_location, 32));

        mapView.getOverlays().add(startMarker);
        mapView.invalidate();
    }

    private void addDestinationMarker(double lat, double lon, String title) {
        if (mapView == null) return;

        if (destinationMarker != null) {
            mapView.getOverlays().remove(destinationMarker);
        }

        destinationMarker = new Marker(mapView);
        destinationMarker.setPosition(new GeoPoint(lat, lon));
        destinationMarker.setTitle(title);
        destinationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        destinationMarker.setIcon(resizeMarkerIcon(R.drawable.destination_red_dot, 32));


        mapView.getOverlays().add(destinationMarker);
        mapView.invalidate();
    }

    private void initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        locationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            Boolean fine = permissions.get(Manifest.permission.ACCESS_FINE_LOCATION);
            Boolean coarse = permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (Boolean.TRUE.equals(fine) || Boolean.TRUE.equals(coarse)) getCurrentLocation();
            else Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
        });
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    // Set current location as starting point
                    selectedStartLocation = new LocationSuggestion(
                            "My current location",
                            "Current GPS location",
                            lat,
                            lon
                    );

                    latitude = lat;
                    longitude = lon;

                    // Save current coordinates
                    sharedPreferences.edit()
                            .putLong(DataBaseConstant.CURRENT_POSITION_LATITUDE, Double.doubleToRawLongBits(lat))
                            .putLong(DataBaseConstant.CURRENT_POSITION_LONGITUDE, Double.doubleToRawLongBits(lon))
                            .apply();

                    // Update map
                    mapView.getController().animateTo(new GeoPoint(lat, lon));
                    mapView.getController().setZoom(17.0);
                    addStartMarker(lat, lon, "Current Location");
                    mapView.invalidate();

                    // Update dialog to destination search mode
                    if (searchBottomSheet != null && searchBottomSheet.isShowing() && searchBinding != null) {
                        searchBinding.tvStartLocation.setText("My current location");
                        searchingStartLocation = false;

                        searchBinding.tvSearchTarget.setText("Search destination");
                        searchBinding.etLocationSearch.setText("");
                        searchBinding.etLocationSearch.setHint("Search destination...");
                        searchBinding.tvLocationSearchStatus.setText("Now select your destination");
                        searchBinding.locationSuggestionList.setAdapter(null);
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Location error: " + e.getMessage());
                    Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show();
                });
    }

    private void openLocationSearch() {
        searchBottomSheet = new BottomSheetDialog(requireContext());
        searchBinding = UserLocationSearchDialogBinding.inflate(getLayoutInflater());
        searchBottomSheet.setContentView(searchBinding.getRoot());

        searchBottomSheet.setOnShowListener(dialog -> {
            View sheet = searchBottomSheet.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet
            );

            if (sheet != null) {
                sheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                sheet.requestLayout();

                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(false); // Disable dragging
                behavior.setHideable(false);  // Prevent swipe-to-dismiss
            }
        });

        searchBinding.btnCloseSearch.setOnClickListener(v -> searchBottomSheet.dismiss());
        searchBinding.tvUseCurrentLocation.setOnClickListener(v -> getCurrentLocation());

        setupRouteLocationFields();
        setupLocationSearch();
        searchBottomSheet.show();
    }

    private void setupLocationSearch() {
        searchBinding.etLocationSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchLocations(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) { }
        });
    }

    private void searchLocations(String query) {
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        int requestId = ++searchRequestId;
        String searchText = query.trim();

        if (searchText.length() < 3) {
            searchBinding.locationSearchProgress.setVisibility(View.GONE);
            searchBinding.tvLocationSearchStatus.setText("Type at least 3 characters");
            searchBinding.locationSuggestionList.setAdapter(null);
            return;
        }

        searchBinding.locationSearchProgress.setVisibility(View.VISIBLE);
        searchBinding.tvLocationSearchStatus.setText("Searching locations...");

        searchRunnable = () -> fetchLocationSuggestions(searchText, requestId);
        searchHandler.postDelayed(searchRunnable, 1000);
    }

    private void fetchLocationSuggestions(String query, int requestId) {
        searchExecutor.execute(() -> {
            ArrayList<LocationSuggestion> results = new ArrayList<>();
            HttpURLConnection connection = null;

            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                URL url = new URL("https://nominatim.openstreetmap.org/search?format=jsonv2&limit=6&q=" + encodedQuery);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("User-Agent", "EcoRoute/1.0 (Android; com.example.ecoroute)");
                connection.setRequestProperty("Accept", "application/json");

                StringBuilder response = new StringBuilder();
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String name = object.optString("name", "");
                    String address = object.optString("display_name", "");
                    double lat = object.optDouble("lat");
                    double lon = object.optDouble("lon");
                    if (name.trim().isEmpty()) name = address.split(",")[0];
                    results.add(new LocationSuggestion(name, address, lat, lon));
                }
            } catch (Exception e) {
                Log.e(TAG, "Location search API error: " + e.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
            }

            ArrayList<LocationSuggestion> finalResults = results;
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                if (!isViewActive || searchBinding == null || requestId != searchRequestId) return;
                searchBinding.locationSearchProgress.setVisibility(View.GONE);
                showLocationSuggestions(finalResults);
            });
        });
    }

    private void showLocationSuggestions(ArrayList<LocationSuggestion> results) {
        ListView listView = searchBinding.locationSuggestionList;

        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() { return results.size(); }

            @Override
            public Object getItem(int position) { return results.get(position); }

            @Override
            public long getItemId(int position) { return position; }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = convertView;
                if (row == null) row = LayoutInflater.from(requireContext()).inflate(R.layout.item_location_suggestion, parent, false);

                LocationSuggestion item = results.get(position);
                TextView name = row.findViewById(R.id.tvSuggestionName);
                TextView address = row.findViewById(R.id.tvSuggestionAddress);
                name.setText(item.getName());
                address.setText(item.getAddress());
                return row;
            }
        };

        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> selectDestination(results.get(position)));
        searchBinding.tvLocationSearchStatus.setText(results.isEmpty() ? "No locations found" : "Select a location");
    }

    private void selectDestination(LocationSuggestion item) {

        if (searchingStartLocation) {
            // Save selected starting point
            selectedStartLocation = item;

            searchBinding.tvStartLocation.setText(item.getName());

            addStartMarker(
                    item.getLatitude(),
                    item.getLongitude(),
                    item.getName()
            );

        } else {
            // Save selected destination
            selectedDestination = item;

            searchBinding.tvDestinationLocation.setText(item.getName());

            addDestinationMarker(
                    item.getLatitude(),
                    item.getLongitude(),
                    item.getName()
            );
        }

        searchBinding.etLocationSearch.setText("");

        mapView.getController().animateTo(
                new GeoPoint(item.getLatitude(), item.getLongitude())
        );

        mapView.getController().setZoom(17.0);
        mapView.invalidate();

        searchBinding.tvLocationSearchStatus.setText("Location selected");
    }

    private void setupRouteLocationFields() {
        searchBinding.tvStartLocation.setOnClickListener(v -> {
            searchingStartLocation = true;
            searchBinding.tvSearchTarget.setText("Search starting point");
            searchBinding.etLocationSearch.setText("");
            searchBinding.etLocationSearch.setHint("Search starting point...");
            searchBinding.locationSuggestionList.setAdapter(null);
        });

        searchBinding.tvDestinationLocation.setOnClickListener(v -> {
            searchingStartLocation = false;
            searchBinding.tvSearchTarget.setText("Search destination");
            searchBinding.etLocationSearch.setText("");
            searchBinding.etLocationSearch.setHint("Search destination...");
            searchBinding.locationSuggestionList.setAdapter(null);
        });

        searchBinding.btnFindShortestRoute.setOnClickListener(v -> calculateShortestDistance());
    }

    private void calculateShortestDistance() {
        if (selectedStartLocation == null || selectedDestination == null) {
            Toast.makeText(requireContext(), "Select starting point and destination", Toast.LENGTH_SHORT).show();
            return;
        }

        double startLat = selectedStartLocation.getLatitude();
        double startLon = selectedStartLocation.getLongitude();
        double destLat = selectedDestination.getLatitude();
        double destLon = selectedDestination.getLongitude();

        addStartMarker(startLat, startLon, selectedStartLocation.getName());
        addDestinationMarker(destLat, destLon, selectedDestination.getName());

        searchBinding.tvRouteDistance.setText("Finding road route...");

        routingExecutor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                String urlString = "https://router.project-osrm.org/route/v1/driving/"
                        + startLon + "," + startLat + ";"
                        + destLon + "," + destLat
                        + "?overview=full&geometries=geojson&steps=true";

                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("User-Agent", "EcoRoute Android");

                StringBuilder response = new StringBuilder();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());

                if (!"Ok".equals(json.optString("code"))) {
                    throw new Exception("Route not found");
                }

                JSONArray routes = json.getJSONArray("routes");
                JSONObject route = routes.getJSONObject(0);

                double distanceMeters = route.getDouble("distance");
                double durationSeconds = route.getDouble("duration");

                JSONArray coordinates = route
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates");

                ArrayList<GeoPoint> roadPoints = new ArrayList<>();

                for (int i = 0; i < coordinates.length(); i++) {
                    JSONArray point = coordinates.getJSONArray(i);

                    double lon = point.getDouble(0);
                    double lat = point.getDouble(1);

                    roadPoints.add(new GeoPoint(lat, lon));
                }

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || mapView == null || searchBinding == null) return;

                    drawRoadRoute(roadPoints);

                    String distance = distanceMeters >= 1000
                            ? String.format(java.util.Locale.getDefault(), "%.2f km", distanceMeters / 1000.0)
                            : String.format(java.util.Locale.getDefault(), "%.0f m", distanceMeters);

                    String duration = formatDuration(durationSeconds);

                    searchBinding.tvRouteDistance.setText(
                            "Road distance: " + distance + "\nEstimated time: " + duration
                    );

                    zoomToRoute(roadPoints);
                });

            } catch (Exception e) {
                Log.e(TAG, "Routing error", e);

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (searchBinding != null) {
                            searchBinding.tvRouteDistance.setText("Unable to find road route");
                        }
                        Toast.makeText(requireContext(), "Route could not be loaded", Toast.LENGTH_SHORT).show();
                    });
                }
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private void drawRoadRoute(ArrayList<GeoPoint> roadPoints) {
        if (mapView == null || roadPoints == null || roadPoints.isEmpty()) return;

        if (routePolyline != null) {
            mapView.getOverlays().remove(routePolyline);
        }

        routePolyline = new Polyline();
        routePolyline.setPoints(roadPoints);
        routePolyline.setWidth(12f);
        routePolyline.setColor(ContextCompat.getColor(requireContext(), R.color.button_primary));

        mapView.getOverlays().add(routePolyline);
        mapView.invalidate();
    }

    private void zoomToRoute(ArrayList<GeoPoint> points) {
        if (mapView == null || points == null || points.isEmpty()) return;

        org.osmdroid.util.BoundingBox box =
                org.osmdroid.util.BoundingBox.fromGeoPoints(points);

        mapView.post(() -> mapView.zoomToBoundingBox(box, true, 100));
    }

    private String formatDuration(double seconds) {
        int totalMinutes = (int) (seconds / 60);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        if (hours > 0) {
            return hours + " hr " + minutes + " min";
        }
        return minutes + " min";
    }

    private Drawable resizeMarkerIcon(int drawableId, int sizeDp) {
        Drawable drawable = ContextCompat.getDrawable(requireContext(), drawableId);
        if (drawable == null) return null;

        int sizePx = (int) (sizeDp * getResources().getDisplayMetrics().density);

        Bitmap bitmap = Bitmap.createBitmap(
                sizePx,
                sizePx,
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, sizePx, sizePx);
        drawable.draw(canvas);

        return new BitmapDrawable(getResources(), bitmap);
    }

    private final SensorEventListener rotationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (mapView == null) return;

            float[] rotationMatrix = new float[9];
            float[] orientation = new float[3];

            SensorManager.getRotationMatrixFromVector(
                    rotationMatrix,
                    event.values
            );

            SensorManager.getOrientation(rotationMatrix, orientation);

            float azimuth = (float) Math.toDegrees(orientation[0]);

            // Normalize bearing from 0 to 360
            azimuth = (azimuth + 360) % 360;

            mapView.setMapOrientation(-azimuth);
            mapView.invalidate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        if (mapView != null) {
            mapView.onResume();
        }

        if (sensorManager != null && rotationSensor != null) {
            sensorManager.registerListener(
                    rotationListener,
                    rotationSensor,
                    SensorManager.SENSOR_DELAY_UI
            );
        }
    }

    @Override
    public void onPause() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(rotationListener);
        }

        if (mapView != null) {
            mapView.onPause();
        }

        super.onPause();
    }

    @Override
    public void onDestroyView() {
        isViewActive = false;
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        searchRequestId++;
        if (searchBottomSheet != null) {
            searchBottomSheet.dismiss();
            searchBottomSheet = null;
        }
        if (mapView != null) {
            mapView.onDetach();
            mapView = null;
        }
        searchBinding = null;
        binding = null;
        super.onDestroyView();
    }
}
