package com1032.cw2.ms01288.ms01288_assignment2;

import android.content.Context;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        MapFragment mf = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mf.getMapAsync(this);

        Intent photoFeed = getIntent();
        photoLatitude = photoFeed.getDoubleExtra("Latitude", 0);
        photoLongitude = photoFeed.getDoubleExtra("Longitude", 0);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(loc == null) {
            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        userLatitude = loc.getLatitude();
        userLongitude = loc.getLongitude();
        Log.v("there is a location", userLatitude  + "," + userLongitude);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                new LocationListener() {
                    public void onLocationChanged(Location location) {
                        if(map != null) {
                            map.addMarker(new MarkerOptions()
                                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .title("New position"));
                        }
                            float[] results = new float[1];
                            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                    userLatitude, userLongitude,
                                    results);
                            Toast.makeText(getApplicationContext(), results + "", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override
                    public void onProviderEnabled(String provider) {}
                    @Override
                    public void onProviderDisabled(String provider) {}
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
}
