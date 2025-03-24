package com.team7.locationaware;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationFragment extends Fragment {

    // Constants for the argument keys
    private static final String ARG_LAT = "latitude";
    private static final String ARG_LON = "longitude";

    // Location parameters
    private double lat;
    private double lon;

    // UI elements
    private TextView txtCoordinates;

    // Handler for periodic updates
    private Handler locationUpdateHandler;
    private static final long UPDATE_INTERVAL = 5000; // 5 seconds

    public LocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param latitude Parameter 1.
     * @param longitude Parameter 2.
     * @return A new instance of fragment LocationFragment.
     */
    public static LocationFragment newInstance(double latitude, double longitude) {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LAT, latitude);
        args.putDouble(ARG_LON, longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lat = getArguments().getDouble(ARG_LAT);
            lon = getArguments().getDouble(ARG_LON);
        }

        // Initialize the handler for periodic updates
        locationUpdateHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        // Get reference to the TextView for coordinates
        txtCoordinates = view.findViewById(R.id.txt_coordinates);

        // Update the coordinates display
        updateCoordinatesDisplay();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start periodic location updates
        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop periodic location updates when fragment is not visible
        stopLocationUpdates();
    }

    /**
     * Updates the coordinates in the UI.
     */
    public void updateCoordinates(double latitude, double longitude) {
        this.lat = latitude;
        this.lon = longitude;

        updateCoordinatesDisplay();
    }

    /**
     * Updates the TextView with the current coordinates.
     */
    private void updateCoordinatesDisplay() {
        if (txtCoordinates != null) {
            String coordinatesText = String.format("%.6f, %.6f", lat, lon);
            txtCoordinates.setText(coordinatesText);
        }
    }

    /**
     * Starts periodic location updates.
     */
    private void startLocationUpdates() {
        locationUpdateHandler.postDelayed(locationUpdateRunnable, UPDATE_INTERVAL);
    }

    /**
     * Stops periodic location updates.
     */
    private void stopLocationUpdates() {
        locationUpdateHandler.removeCallbacks(locationUpdateRunnable);
    }

    /**
     * Runnable that requests location updates.
     * This will be executed every UPDATE_INTERVAL milliseconds.
     */
    private Runnable locationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            // Request location update from MainActivity
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).requestLocationUpdate();
            }

            // Schedule the next update
            locationUpdateHandler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    /**
     * Returns the current latitude.
     */
    public double getLatitude() {
        return lat;
    }

    /**
     * Returns the current longitude.
     */
    public double getLongitude() {
        return lon;
    }
}