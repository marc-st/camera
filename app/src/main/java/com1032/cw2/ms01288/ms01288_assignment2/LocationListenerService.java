package com1032.cw2.ms01288.ms01288_assignment2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class LocationListenerService extends Service {
    public LocationManager locationManager;
    public MyLocationListener listener;
    public static final String LOCATION_RECEIVED = "LOCATION_RECEIVED";

    Intent intent;

    @Override
    public void onCreate() {
        super.onCreate();
        // initialise intent to be sent back via BroadcastReceiver
        intent = new Intent(LOCATION_RECEIVED);
    }

    /** same as onStart (now deprecated for Service)
     *  initialise location manager and set up
     *  listeners for changes in location
     *
     * @param intent
     * @param flags
     * @param startId
     * @return int
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // no longer listen for changes
        locationManager.removeUpdates(listener);
    }

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location loc) {

            // put latitude and longitude inside intents
            intent.putExtra("Latitude", loc.getLatitude());
            intent.putExtra("Longitude", loc.getLongitude());
            // send broadcast back to Map
            sendBroadcast(intent);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}

    }
}