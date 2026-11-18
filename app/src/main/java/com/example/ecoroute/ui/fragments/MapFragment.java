package com.example.ecoroute.ui.fragments;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
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
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/** Commit 1: route selection, OSRM path retrieval and route display only. */
public class MapFragment extends Fragment {
    private static final String TAG = "EcoRouteMap";
    private FragmentMapBinding binding;
    private UserLocationSearchDialogBinding searchBinding;
    private SharedPreferences preferences;
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private Marker startMarker, destinationMarker;
    private Polyline routePolyline;
    private ArrayList<GeoPoint> activeRoutePoints = new ArrayList<>();
    private LocationSuggestion selectedStartLocation, selectedDestination;
    private boolean searchingStartLocation = true;
    private BottomSheetDialog searchDialog;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService searchExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService routeExecutor = Executors.newSingleThreadExecutor();
    private Runnable searchRunnable;
    private int searchRequestId = 0;
    private boolean viewActive = false;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewActive = true;
        preferences = requireContext().getSharedPreferences(DataBaseConstant.DATABASE_NAME, Context.MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                    || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION))) {
                getCurrentLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        });
        initializeMap();
        ScreenArrangement.adjustStatusBarHeight(binding.viewStatusBarMapFragment);
        binding.btnCurrentLocation.setOnClickListener(v -> openLocationSearch());
    }


    private void initializeMap() {
        Configuration.getInstance().setUserAgentValue("EcoRoute/1.0 (Android; com.example.ecoroute)");
        Configuration.getInstance().setDebugMode(false);
        Configuration.getInstance().setDebugMapView(false);
        mapView = binding.osmMap;
        mapView.setUseDataConnection(true);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        mapView.setMinZoomLevel(6.0);
        mapView.setMaxZoomLevel(26.0);
        // Standard OSM raster tiles; tile labels are controlled by the tile provider.
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        loadSavedPosition();
        mapView.getController().setZoom(16.0);
        mapView.getController().setCenter(new GeoPoint(latitude, longitude));
        addStartMarker(latitude, longitude, "Current Location");
    }


    private double latitude = 21.1702, longitude = 72.8311;
    private void loadSavedPosition() {
        latitude = Double.longBitsToDouble(preferences.getLong(DataBaseConstant.CURRENT_POSITION_LATITUDE,
                Double.doubleToRawLongBits(21.1702)));
        longitude = Double.longBitsToDouble(preferences.getLong(DataBaseConstant.CURRENT_POSITION_LONGITUDE,
                Double.doubleToRawLongBits(72.8311)));
    }


    private void openLocationSearch() {
        searchDialog = new BottomSheetDialog(requireContext());
        searchBinding = UserLocationSearchDialogBinding.inflate(getLayoutInflater());
        searchDialog.setContentView(searchBinding.getRoot());
        searchDialog.setOnShowListener(dialog -> {
            View sheet = searchDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet != null) {
                sheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                sheet.requestLayout();
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(false);
                behavior.setHideable(false);
            }
        });
        searchBinding.btnCloseSearch.setOnClickListener(v -> searchDialog.dismiss());
        searchBinding.tvUseCurrentLocation.setOnClickListener(v -> getCurrentLocation());
        setupRouteFields();
        setupSearchTextListener();
        searchDialog.show();
    }


    private void setupRouteFields() {
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
        searchBinding.btnFindShortestRoute.setOnClickListener(v -> calculateRoadRoute());
    }


    private void setupSearchTextListener() {
        searchBinding.etLocationSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { searchLocations(s.toString()); }
            @Override public void afterTextChanged(android.text.Editable s) { }
        });
    }


    private void searchLocations(String text) {
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        int requestId = ++searchRequestId;
        String query = text.trim();
        if (query.length() < 3) {
            searchBinding.locationSearchProgress.setVisibility(View.GONE);
            searchBinding.tvLocationSearchStatus.setText("Type at least 3 characters");
            searchBinding.locationSuggestionList.setAdapter(null);
            return;
        }
        searchBinding.locationSearchProgress.setVisibility(View.VISIBLE);
        searchBinding.tvLocationSearchStatus.setText("Searching locations...");
        searchRunnable = () -> fetchSuggestions(query, requestId);
        searchHandler.postDelayed(searchRunnable, 700);
    }


    private void fetchSuggestions(String query, int requestId) {
        searchExecutor.execute(() -> {
            ArrayList<LocationSuggestion> results = new ArrayList<>();
            HttpURLConnection connection = null;
            try {
                String encoded = URLEncoder.encode(query, "UTF-8");
                URL url = new URL("https://nominatim.openstreetmap.org/search?format=jsonv2&limit=6&q=" + encoded);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("User-Agent", "EcoRoute/1.0 (Android; com.example.ecoroute)");
                connection.setRequestProperty("Accept", "application/json");
                StringBuilder response = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                JSONArray array = new JSONArray(response.toString());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    String name = item.optString("name", "");
                    String address = item.optString("display_name", "");
                    if (name.trim().isEmpty()) name = address.split(",")[0];
                    results.add(new LocationSuggestion(name, address, item.optDouble("lat"), item.optDouble("lon")));
                }
            } catch (Exception e) {
                Log.e(TAG, "Place search failed", e);
            } finally {
                if (connection != null) connection.disconnect();
            }
            ArrayList<LocationSuggestion> found = results;
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                if (!viewActive || searchBinding == null || requestId != searchRequestId) return;
                searchBinding.locationSearchProgress.setVisibility(View.GONE);
                displaySuggestions(found);
            });
        });
    }


    private void displaySuggestions(ArrayList<LocationSuggestion> results) {
        ListView list = searchBinding.locationSuggestionList;
        BaseAdapter adapter = new BaseAdapter() {
            @Override public int getCount() { return results.size(); }
            @Override public Object getItem(int position) { return results.get(position); }
            @Override public long getItemId(int position) { return position; }
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                View row = convertView;
                if (row == null) row = LayoutInflater.from(requireContext()).inflate(R.layout.item_location_suggestion, parent, false);
                LocationSuggestion item = results.get(position);
                ((TextView) row.findViewById(R.id.tvSuggestionName)).setText(item.getName());
                ((TextView) row.findViewById(R.id.tvSuggestionAddress)).setText(item.getAddress());
                return row;
            }
        };
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> selectLocation(results.get(position)));
        searchBinding.tvLocationSearchStatus.setText(results.isEmpty() ? "No locations found" : "Select a location");
    }


    private void selectLocation(LocationSuggestion item) {
        if (searchingStartLocation) {
            selectedStartLocation = item;
            searchBinding.tvStartLocation.setText(item.getName());
            addStartMarker(item.getLatitude(), item.getLongitude(), item.getName());
        } else {
            selectedDestination = item;
            searchBinding.tvDestinationLocation.setText(item.getName());
            addDestinationMarker(item.getLatitude(), item.getLongitude(), item.getName());
        }
        searchBinding.etLocationSearch.setText("");
        searchBinding.tvLocationSearchStatus.setText("Location selected");
        mapView.getController().animateTo(new GeoPoint(item.getLatitude(), item.getLongitude()));
        mapView.getController().setZoom(17.0);
        hideKeyboard();
    }


    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }
        CancellationTokenSource token = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.getToken())
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    selectedStartLocation = new LocationSuggestion("My current location", "Current GPS location", latitude, longitude);
                    preferences.edit()
                            .putLong(DataBaseConstant.CURRENT_POSITION_LATITUDE, Double.doubleToRawLongBits(latitude))
                            .putLong(DataBaseConstant.CURRENT_POSITION_LONGITUDE, Double.doubleToRawLongBits(longitude)).apply();
                    addStartMarker(latitude, longitude, "Current Location");
                    mapView.getController().animateTo(new GeoPoint(latitude, longitude));
                    mapView.getController().setZoom(17.0);
                    if (searchDialog != null && searchDialog.isShowing() && searchBinding != null) {
                        searchBinding.tvStartLocation.setText("My current location");
                        searchingStartLocation = false;
                        searchBinding.tvSearchTarget.setText("Search destination");
                        searchBinding.etLocationSearch.setText("");
                        searchBinding.etLocationSearch.setHint("Search destination...");
                        searchBinding.tvLocationSearchStatus.setText("Now select your destination");
                        searchBinding.locationSuggestionList.setAdapter(null);
                    }
                }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show());
    }


    private void calculateRoadRoute() {
        if (selectedStartLocation == null || selectedDestination == null) {
            Toast.makeText(requireContext(), "Select starting point and destination", Toast.LENGTH_SHORT).show();
            return;
        }
        final double startLat = selectedStartLocation.getLatitude(), startLon = selectedStartLocation.getLongitude();
        final double destLat = selectedDestination.getLatitude(), destLon = selectedDestination.getLongitude();
        addStartMarker(startLat, startLon, selectedStartLocation.getName());
        addDestinationMarker(destLat, destLon, selectedDestination.getName());
        searchBinding.tvRouteDistance.setText("Finding road route...");
        routeExecutor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String endpoint = "https://router.project-osrm.org/route/v1/driving/" + startLon + "," + startLat + ";" + destLon + "," + destLat
                        + "?overview=full&geometries=geojson&steps=true";
                connection = (HttpURLConnection) new URL(endpoint).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("User-Agent", "EcoRoute Android");
                StringBuilder response = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                JSONObject root = new JSONObject(response.toString());
                if (!"Ok".equals(root.optString("code"))) throw new Exception("Route not found");
                JSONObject route = root.getJSONArray("routes").getJSONObject(0);
                double distance = route.getDouble("distance"), duration = route.getDouble("duration");
                JSONArray coords = route.getJSONObject("geometry").getJSONArray("coordinates");
                ArrayList<GeoPoint> points = new ArrayList<>();
                for (int i = 0; i < coords.length(); i++) {
                    JSONArray p = coords.getJSONArray(i);
                    points.add(new GeoPoint(p.getDouble(1), p.getDouble(0)));
                }
                if (isAdded()) requireActivity().runOnUiThread(() -> {
                    if (!viewActive || mapView == null || searchBinding == null) return;
                    activeRoutePoints = new ArrayList<>(points);
                    drawRoute(points);
                    String distanceText = distance >= 1000 ? String.format(Locale.US, "%.2f km", distance / 1000.0)
                            : String.format(Locale.US, "%.0f m", distance);
                    searchBinding.tvRouteDistance.setText("Road distance: " + distanceText + "\nEstimated time: " + formatDuration(duration));
                    zoomToRoute(points);
                    hideKeyboard();
                });
            } catch (Exception e) {
                Log.e(TAG, "Route request failed", e);
                if (isAdded()) requireActivity().runOnUiThread(() -> {
                    if (searchBinding != null) searchBinding.tvRouteDistance.setText("Unable to find road route");
                    Toast.makeText(requireContext(), "Route could not be loaded", Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }


    private void drawRoute(ArrayList<GeoPoint> points) {
        if (mapView == null || points == null || points.isEmpty()) return;
        if (routePolyline != null) mapView.getOverlays().remove(routePolyline);
        routePolyline = new Polyline();
        routePolyline.setPoints(points);
        routePolyline.setWidth(12f);
        routePolyline.setColor(ContextCompat.getColor(requireContext(), R.color.button_primary));
        mapView.getOverlays().add(routePolyline);
        mapView.invalidate();
    }


    private void zoomToRoute(ArrayList<GeoPoint> points) {
        if (mapView == null || points == null || points.isEmpty()) return;
        org.osmdroid.util.BoundingBox bounds = org.osmdroid.util.BoundingBox.fromGeoPoints(points);
        mapView.post(() -> mapView.zoomToBoundingBox(bounds, true, 100));
    }


    private void addStartMarker(double lat, double lon, String title) {
        if (mapView == null) return;
        if (startMarker != null) mapView.getOverlays().remove(startMarker);
        startMarker = new Marker(mapView);
        startMarker.setPosition(new GeoPoint(lat, lon));
        startMarker.setTitle(title);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.current_location));
        mapView.getOverlays().add(startMarker);
        mapView.invalidate();
    }


    private void addDestinationMarker(double lat, double lon, String title) {
        if (mapView == null) return;
        if (destinationMarker != null) mapView.getOverlays().remove(destinationMarker);
        destinationMarker = new Marker(mapView);
        destinationMarker.setPosition(new GeoPoint(lat, lon));
        destinationMarker.setTitle(title);
        destinationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        destinationMarker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.destination_red_dot));
        mapView.getOverlays().add(destinationMarker);
        mapView.invalidate();
    }


    private String formatDuration(double seconds) {
        int minutesTotal = (int) (seconds / 60);
        int hours = minutesTotal / 60, minutes = minutesTotal % 60;
        return hours > 0 ? hours + " hr " + minutes + " min" : minutes + " min";
    }


    private void hideKeyboard() {
        if (searchBinding == null) return;
        EditText input = searchBinding.etLocationSearch;
        InputMethodManager manager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) manager.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }


    @Override public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override public void onPause() {
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override public void onDestroyView() {
        viewActive = false;
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        searchRequestId++;
        if (searchDialog != null) {
            searchDialog.dismiss();
            searchDialog = null;
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
