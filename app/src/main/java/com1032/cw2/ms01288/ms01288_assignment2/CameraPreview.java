package com1032.cw2.ms01288.ms01288_assignment2;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import java.io.IOException;

import static android.content.ContentValues.TAG;



/**
 * Created by marcstevens on 24/04/2017.
 */

@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mCamera.setDisplayOrientation(90);

        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.v("surfaceCreated", "surface is created");
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v("surfaceDestroyed", "surface is destroyed");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.v("surface Changed", "rotation");

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            Log.v("NO PREVIEW", "Tried to close non-existent preview");
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

}
