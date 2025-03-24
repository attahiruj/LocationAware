package com.team7.locationaware;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    // Constants
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int SMS_PERMISSION_REQUEST_CODE = 1002;
    private static final String PHONE_NUMBER = "+250796179524";

    // Location components
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    // UI Components
    private Button btnFindMe;
    private Button btnTextMe;
    private LocationFragment locationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up UI components
        btnFindMe = findViewById(R.id.btnFindMe);
        btnTextMe = findViewById(R.id.btnTextMe);

        // Get reference to the location fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        locationFragment = (LocationFragment) fragmentManager.findFragmentById(R.id.fragmentContainerView);

        // Initialize location request
        createLocationRequest();

        // Initialize location callback
        createLocationCallback();

        // Set up button click listeners
        setupButtonListeners();

        // Request initial location
        getLastLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check permissions and start location updates if permissions granted
        if (checkLocationPermission()) {
            requestLocationUpdate();
        } else {
            requestLocationPermission();
        }
    }

    /**
     * Sets up click listeners for the buttons.
     */
    private void setupButtonListeners() {
        btnFindMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocationInGoogleMaps();
            }
        });

        btnTextMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSmsPermission()) {
                    sendLocationSms();
                } else {
                    requestSmsPermission();
                }
            }
        });
    }

    /**
     * Creates the location request
     */
    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(5000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(5000)
                .build();
    }

    /**
     * Creates the location callback that handles location updates.
     */
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update location in fragment
                    if (locationFragment != null) {
                        locationFragment.updateCoordinates(
                                location.getLatitude(),
                                location.getLongitude()
                        );
                    }
                }
            }
        };
    }

    /**
     * Retrieves the last known location.
     */
    private void getLastLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null && locationFragment != null) {
                                locationFragment.updateCoordinates(
                                        location.getLatitude(),
                                        location.getLongitude()
                                );
                            }
                        }
                    });
        } else {
            requestLocationPermission();
        }
    }

    /**
     * Requests a location update.
     * Called by LocationFragment every 5 seconds.
     */
    public void requestLocationUpdate() {
        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        }
    }

    /**
     * Opens the current location in Google Maps.
     */
    private void openLocationInGoogleMaps() {
        if (locationFragment != null) {
            double latitude = locationFragment.getLatitude();
            double longitude = locationFragment.getLongitude();

            try {
                // Try a more reliable approach to open Google Maps
                String uri = String.format("https://www.google.com/maps/search/?api=1&query=%f,%f", latitude, longitude);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                // Fallback to geo URI if the first approach fails
                try {
                    Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    startActivity(mapIntent);
                } catch (Exception ex) {
                    Toast.makeText(this, "Error opening maps: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Sends an SMS with the current location.
     */
    private void sendLocationSms() {
        if (locationFragment != null) {
            double latitude = locationFragment.getLatitude();
            double longitude = locationFragment.getLongitude();

            String message = "My current location is: " + latitude + ", " + longitude +
                    "\nGoogle Maps: https://maps.google.com/?q=" + latitude + "," + longitude;

            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(PHONE_NUMBER, null, message, null, null);
                Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "SMS failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if location permission is granted.
     */
    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests location permission.
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    /**
     * Checks if SMS permission is granted.
     */
    private boolean checkSmsPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests SMS permission.
     */
    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, send SMS
                sendLocationSms();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates when activity is not in foreground
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}