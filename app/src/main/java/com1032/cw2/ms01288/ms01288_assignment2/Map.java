package com1032.cw2.ms01288.ms01288_assignment2;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Timer;
import java.util.TimerTask;

public class Map extends Activity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private GoogleMap map = null;

    private double photoLatitude;
    private double photoLongitude;

    private double userLatitude;
    private double userLongitude;

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeListener mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        MapFragment mf = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        // initialize the map's system and view
        mf.getMapAsync(this);

        /** get intent extras from PhotoFeed
         * that contain the latitude and longitude coordinates
         */
        Intent photoFeed = getIntent();
        double[] coordinates = photoFeed.getDoubleArrayExtra("coordinates");
        photoLatitude = coordinates[0];
        photoLongitude = coordinates[1];

        // setup an instances of LocationManager and Location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(loc == null) {
            // use the devices network connection instead of GPS
            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        // retrieve latitude and longitude coordinates of device from Location
        userLatitude = loc.getLatitude();
        userLongitude = loc.getLongitude();

        // listen for changes in the device's location
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                new LocationListener() {
                    public void onLocationChanged(Location location) {
                        if(map != null) {
                            // add a map marker at the new position
                            map.addMarker(new MarkerOptions()
                                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .title("New position"));
                        }
                            float[] results = new float[1];
                            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                    userLatitude, userLongitude,
                                    results);
                            Toast.makeText(getApplicationContext(),
                                    results +  "away from where picture was taken", Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override
                    public void onProviderEnabled(String provider) {}
                    @Override
                    public void onProviderDisabled(String provider) {}
                });
        // SensorManager and ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeListener();
        // Listen for when user's shakes device
        mShakeDetector.setOnShakeListener(new ShakeListener.OnShakeListener() {

            @Override
            public void onShake(int count) {
                /**
                 * SHAKE EVENT
                 * When shake is recorded, reset the maps position
                 * to the user's coordinates and reset to default zoom
                 */
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLatitude, userLongitude), 14.0f));
            }
        });

    }
    @Override
    public void onMapLoaded() {
        map.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;

        map.addMarker(new MarkerOptions()
        .position(new LatLng(photoLatitude, photoLongitude))
        .title("Taken here"));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(photoLatitude, photoLongitude), 14.0f));

        map.addMarker(new MarkerOptions()
        .position(new LatLng(userLatitude, userLongitude))
        .title("Your position"));
    }
    @Override
    public void onResume() {
        super.onResume();
        // Register the Sensor Manager Listener when map is re-created
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }
}
