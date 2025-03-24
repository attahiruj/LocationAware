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

//define the class fragment used for dynamically manipulating the location.
public class LocationFragment extends Fragment {

    //constants for the argument keys
    private static final String ARG_LAT = "latitude";
    private static final String ARG_LON = "longitude";

    //location parameters
    private double lat;
    private double lon;

    //UI elements
    private TextView txtCoordinates;

    //handler for periodic updates
    private Handler locationUpdateHandler;
    private static final long UPDATE_INTERVAL = 5000; // 5 seconds

    public LocationFragment() {
        //required empty public constructor
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

        //initialize the handler for periodic updates
        locationUpdateHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        //get reference to the TextView for coordinates
        txtCoordinates = view.findViewById(R.id.txt_coordinates);

        //update the coordinates display
        updateCoordinatesDisplay();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //start periodic location updates
        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Updates the coordinates in the UI.
     */

    public void updateCoordinates(double latitude, double longitude) {
        //update the value of the coordinates
        this.lat = latitude;
        this.lon = longitude;

        //update the view from the logic.
        updateCoordinatesDisplay();
    }

    /**
     * Helper function: updates the TextView with the current coordinates.
     */
    private void updateCoordinatesDisplay() {
        if (txtCoordinates != null) {
            String coordinatesText = String.format("%.6f, %.6f", lat, lon);
            txtCoordinates.setText(coordinatesText);
        }
    }

    /**
     * Helper function: starts periodic location updates.
     */
    private void startLocationUpdates() {
        locationUpdateHandler.postDelayed(locationUpdateRunnable, UPDATE_INTERVAL);
    }

    /**
     * Helper function: stops periodic location updates.
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
     * Helper function: Returns the current latitude.
     */
    public double getLatitude() {
        return lat;
    }

    /**
     * Helper function: Returns the current longitude.
     */
    public double getLongitude() {
        return lon;
    }
}