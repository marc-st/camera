package com1032.cw2.ms01288.ms01288_assignment2;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private SQLiteDatabase.CursorFactory factory = null;
    public static ImageDB imageDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCamera = getCameraInstance();

        // create a ImageDB instance that extends SQLiteOpenHelper
        imageDB = new ImageDB(this, "imageDB", factory, 1);

        // create CameraPreview instance which
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        // add the camera preview to frame layout
        preview.addView(mPreview);

        // interface that supplies image data as a byte array
        final Camera.PictureCallback mPicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                double mLat = 0.0;
                double mLon = 0.0;

                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                // retrieve last known Location from the devices GPS
                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(loc == null){
                    /** retrieve Location from network provider
                     * when GPS can't be retrieved
                     */
                    loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if(loc != null){
                    // retrieve latitude and longitude to be stored in database
                    mLat = loc.getLatitude();
                    mLon = loc.getLongitude();
                }

                // each argument will be stored in a separate column of database
                imageDB.insertData(data, mLat, mLon);
            }
        };
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                mCamera.takePicture(null, null, mPicture);
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera when activity paused
    }
    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;           // set Camera object back to null
            mPreview.getHolder().removeCallback(mPreview);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            // open the Camera
            mCamera = getCameraInstance();
            // recreate the CameraPreview as done in onCreate()
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // open PhotoFeed activity
        Intent feedIntent = new Intent(this,PhotoFeed.class);
        startActivity(feedIntent);
        return true;
    }
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            /** open back camera on devices with 2
             * cameras e.g. most phones
             */
            if(Camera.getNumberOfCameras() == 2){
                c = Camera.open(0);
            }else{
                /** open FRONT camera on devices with one
                  * camera e.g. TABLET
                 * */
                c = Camera.open(1);
            }
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
        return c;
    }
}
