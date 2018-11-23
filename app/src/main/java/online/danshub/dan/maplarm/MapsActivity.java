package online.danshub.dan.maplarm;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker currentMarker = null;
    private int radiusDistance = 200; // Place holder for passing radius in
    private Circle radius = null;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Log.v("Map", "Map Created");

        FloatingActionButton settingsButton = findViewById(R.id.settingsButton);
        FloatingActionButton setLocationButton = findViewById(R.id.setLocation);

        /*
            Button to go to settings menu
         */
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Clicked Settings!", Toast.LENGTH_LONG).show();
                Log.v("User Action", "Button Clicked!");
                Intent settingIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingIntent);
            }
        });

        /*
            Button will call something that tells the app to go into location mode.
         */
        setLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Location Set, I'll wake you up when we get there. ", Toast.LENGTH_LONG).show();
            }
        });
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

        Log.v("Location", "Marker Create Array");
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
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                drawRadius(latLng);
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
        options.strokeColor(Color.BLACK);

        // Fill color of the circle
        options.fillColor(0x30ff0000);

        // Border width of the circle
        options.strokeWidth(2);

        // Adding the circle to the GoogleMap
        radius = mMap.addCircle(options);
    }

    /*
        Method Checks to see if location permission is granted

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }
    */
}
