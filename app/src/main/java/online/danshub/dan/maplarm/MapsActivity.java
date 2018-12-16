package online.danshub.dan.maplarm;

import android.Manifest;
import android.app.Dialog;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MAP";
    private GoogleMap mMap;
    protected static Marker currentMarker = null;
    private int radiusDistance = 200; // Default value
    private int zoomDistance = 13;
    private Circle radius = null;
    private static final String RADUIS_SETTING = "editRadius";
    private static final String ZOOM_SETTING = "zoomDistance";
    private GeofencingClient mGeofencingClient;
    private MediaPlayer alarmSound;
    private FloatingActionButton settingsButton, stopLocationButton, setLocationButton, saveMarkerButton;
    private Boolean TrackingActive;
    private NotificationCompat.Builder trackingNotificaiton;
    private static final String CHANNEL_ID = "MapLarmNotification";
    private static final int notificaitonID = 1200;
    protected static MarkerDatabase db;

    private PendingIntent geoFencesPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        setPreferences();

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Log.v(TAG, "Map Created");

        createButtonsOverlay();

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        db = Room.databaseBuilder(this, MarkerDatabase.class, "maplarm-db").allowMainThreadQueries().build();
    }

    @Override
    public void onResume(){
        super.onResume();
        setPreferences();
    }

    protected void alarmSound() {
        alarmSound = MediaPlayer.create(this, R.raw.oldfashionedschoolbelldanielsimon);

        alarmSound.start();
    }

    /**
     * For android 8.0+ needs to register the notifications channel
     * https://developer.android.com/training/notify-user/build-notification
     */

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Creates a notification for the app to display the users current location..
     *
     */
    private void createNotification(int notificationID, Boolean create) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (create) {
            String coordinates = "Unknown";
            if (currentMarker != null) {
                coordinates = currentMarker.getPosition().toString();
            }

            trackingNotificaiton = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Maplarm Tracking Location")
                    .setContentText("Current Coordinates: " + coordinates)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            notificationManager.notify(notificationID, trackingNotificaiton.build());
        } else {
            notificationManager.cancel(notificationID);
        }
    }


    private PendingIntent createGeofencePendingIntent() {
        Log.v(TAG, "createGeofencePendingIntent");

        if (geoFencesPendingIntent != null) {
            return geoFencesPendingIntent;
        }

        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);

        geoFencesPendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);

        return geoFencesPendingIntent;
    }

    /**
     * Creates the geofence object based off of the current map marker
     * @return Geofence Object, or null if no marker set.
     */
    protected Geofence createGeofencingServicesTest() {

        if (currentMarker != null) {
            return new Geofence.Builder()
                    .setRequestId("TestGeoFence")
                    .setCircularRegion(
                            currentMarker.getPosition().latitude,
                            currentMarker.getPosition().longitude,
                            radiusDistance
                    ).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setExpirationDuration(100000000)
                    .build();
            //return geoFence;
        } else {
            Toast.makeText(getApplicationContext(), "No Marker Set! ", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private GeofencingRequest getGeofencingRequest(Geofence geoFence) {
        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geoFence)
                .build();

        return request;
    }

    private void addGeofence() {
        if(checkLocationPermission()){
            mGeofencingClient.addGeofences(getGeofencingRequest(createGeofencingServicesTest()), createGeofencePendingIntent())
            .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getApplicationContext(), "Tracking Working!?!?! Really I doubt it", Toast.LENGTH_LONG).show(); //TODO Remove this
                    //alarmSound();
                    Log.v(TAG, "Geofence Added");
                    createNotification(notificaitonID, true);
                }
            })
            .addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Could not create Geofence");
                }
            });
        }
    }


    private void removeGeofence() {
        mGeofencingClient.removeGeofences(createGeofencePendingIntent())
            .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.v(TAG, "Geofence Removed");
                    createNotification(notificaitonID, false);
              }
            })
            .addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Could not stop the Tracking");
                }
            });
    }

    /**
     * Method creates buttons on top of layout and controls listeners
     */
    private void createButtonsOverlay() {
        settingsButton = findViewById(R.id.settingsButton);
        setLocationButton = findViewById(R.id.setLocation);
        stopLocationButton = findViewById(R.id.stopLocationButton);
        saveMarkerButton = findViewById(R.id.saveMarkerButton);

        // Hide the buttons from view until needed.
        setLocationButton.hide();
        stopLocationButton.hide();
        saveMarkerButton.hide();

        /*
            Button to go to settings activity when clicked
         */
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("User Action", "Settings Button Clicked!");
                Intent settingIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingIntent);
            }
        });

        /*
            Button will call something that tells the app to go into location mode.
            Currently placeholder for future app functionality
         */
        setLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMarker == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.location_marker_not_set), Toast.LENGTH_LONG).show();
                } else {
                    addGeofence();
                    setLocationButton.hide();
                    stopLocationButton.show();
                    //setLocationButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.));
                    //alarmSound();
                }
            }
        });

        stopLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeGeofence();
                stopLocationButton.hide();
                if (currentMarker != null) {
                    setLocationButton.show();
                }
            }
        });

        saveMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MarkerDialogFragment markerDialogFragment = new MarkerDialogFragment();
                markerDialogFragment.show(getSupportFragmentManager(), "Test");
            }
        });
    }

    /**
     *
     * Method for getting user settings and applying to map.
     */
    protected void setPreferences() {
        // Settings For App.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        radiusDistance = Integer.parseInt(sharedPreferences.getString(MapsActivity.RADUIS_SETTING, "200"));
        Log.v("RADIUS", String.valueOf(radiusDistance));
        zoomDistance = Integer.parseInt(sharedPreferences.getString(MapsActivity.ZOOM_SETTING, "13"));
        Log.v("ZOOM", String.valueOf(zoomDistance));

    }

    /**
     * Requests permission to use the location
     */
    private void requestLocationPermission() {
        if (!checkLocationPermission()) {
            Toast.makeText(getApplicationContext(), getText(R.string.location_permission_required), Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Toast.makeText(getApplicationContext(), getText(R.string.location_permission_granted), Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Method checks if location permission has been granted.
     */
    private Boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        requestLocationPermission();

        // Check if the user has given permission to use location and start tracking current location.
        if (checkLocationPermission()) {

            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            Location lastLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
            // Sets the camera to zoom into the last known location of the user on start.
            if (lastLocation != null) {
                LatLng lastLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, zoomDistance));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(lastLatLng)
                        .zoom(zoomDistance)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            // For later usage.
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.v("LOCATION CHANGED", location.toString());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            mMap.setMyLocationEnabled(true);
        }

        /*
         * Sets the marker and moves the camera to focus on this point.,
         */
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (currentMarker != null) {
                    currentMarker.remove();
                    currentMarker = null;
                }

                //Log.v("Maps", "Lat: " + latLng.latitude + " | Long: " + latLng.longitude + " Markers = " + markers.size());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                //markers.add(mMap.addMarker(new MarkerOptions().position(latLng)));
                currentMarker = mMap.addMarker(new MarkerOptions().position(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomDistance));
                drawRadius(latLng);

                setLocationButton.show();
                saveMarkerButton.show();
                Log.v("Map", "Map Marker Set");
            }
        });
        /*
            Used to remove the map marker from the map
         */
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                currentMarker.remove();
                radius.remove();
                if(setLocationButton != null) {
                    setLocationButton.hide();
                }
                if(saveMarkerButton != null) {
                    saveMarkerButton.hide();
                }
                Log.v("Map", "Map Marker Removed");
            }
        });
    }

    /*
        Method will draw a visible radius around a point, based on LatLng position
     */
    private void drawRadius(LatLng position) {
        if (radius != null) {
            radius.remove();
        }
        CircleOptions options = new CircleOptions();
        options.center(position);

        options.radius(radiusDistance);
        options.strokeColor(getResources().getColor(R.color.colorPrimaryDark));

        // Fill color of the circle
        options.fillColor(getResources().getColor(R.color.colorPrimary));

        // Border width of the circle
        options.strokeWidth(2);

        // Adding the circle to the GoogleMap
        radius = mMap.addCircle(options);
    }
}
