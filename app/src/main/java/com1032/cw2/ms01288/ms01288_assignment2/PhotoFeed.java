package com1032.cw2.ms01288.ms01288_assignment2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;


import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

import static com1032.cw2.ms01288.ms01288_assignment2.MainActivity.imageDB;

public class PhotoFeed extends AppCompatActivity {

    private String[] columnNames = {"IMAGEDATA","LAT","LON"};
    private Cursor images = null;

    public static final String DOWNLOAD_DONE = "DOWNLOAD_DONE";
    private BroadcastReceiver receiver;

    private LinearLayout root;
    private LinearLayout innerLayout;

    private Bitmap bitmap;

    private HashMap<Integer, Bitmap> imageToBitmap = new HashMap<>();
    private ArrayList<Bitmap> bitmapsFromInstanceState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // onCreate is always called on orientation change

        if(getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE){
            // if current orientation is landscape, use landscape layout in XML
            setContentView(R.layout.activity_photo_feed_land);
        }else{
            // if current orientation is portrait, use portrait layout in XML
            setContentView(R.layout.activity_photo_feed);
        }
        SQLiteDatabase db = imageDB.getReadableDatabase();
        // get contents of 'images' table and store it in database
        images = db.query("images", columnNames, null, null, null, null, null);

        root = (LinearLayout) findViewById(R.id.photoFeedLayout); // root LinearLayout
        innerLayout = (LinearLayout) findViewById(R.id.innerLayout); // put image inside this layout

        runThread(); // start a UI thread
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        /**
         * This method is used to store the Bitmap objects
         * this is to save memory so that Bitmap don't need to be re-created
         * every time PhotoFeed activity is recreated
         */

        int counter = 0;
        if(imageToBitmap != null){
            for (Bitmap bm: imageToBitmap.values()) { // iterate through Bitmap objects
                // add each object to Bundle
                savedInstanceState.putParcelable("image" + counter, bm);
                counter++;
            }
        }
        super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        /**
         * This method will get the Bitmap objects stored in the Bundle
         * and add them to the ArrayList. The bitmap objects will be added straight
         * to the ImageView inside UI Thread
         */
        bitmapsFromInstanceState = new ArrayList<>();
        if(savedInstanceState != null){
            for(int i = 0; i < savedInstanceState.size(); i++){
                Bitmap bitmap = savedInstanceState.getParcelable("image" + i);
                bitmapsFromInstanceState.add(bitmap);
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
    private void runThread() {
        new Thread() {
            public void run() {
                if(images.moveToFirst()){
                    try {
                        /** open a UI thread
                         * so that the display can be changed within
                         * the thread
                         */
                        runOnUiThread(new Runnable() {

                            // Array of imageViews
                            ImageView[] imageViews = new ImageView[12];

                            // Map the image's index in the array to it's coordinates
                            HashMap<Integer, double[]> imageToCoords = new HashMap<>();

                            int counter = 0;

                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void run() {
                                do {

                                    final int index = counter;
                                    final double lat = images.getDouble(images.getColumnIndex("LAT"));
                                    final double lon = images.getDouble(images.getColumnIndex("LON"));
                                    final double[] coordinates = new double[]{lat, lon};

                                    /** this if statements runs when
                                     * Activity loaded for first time
                                     */
                                    if(bitmapsFromInstanceState == null){

                                        // get Blob of byte data from Cursor objects
                                        byte[] imData = images.getBlob(images.getColumnIndex("IMAGEDATA"));
                                        BitmapFactory.Options options = new BitmapFactory.Options();

                                        // scale down image to 1/4 of size
                                        options.inSampleSize = 4;

                                        // decode the byte[] array to a bitmap
                                        Bitmap originalBM = BitmapFactory.decodeByteArray(imData, 0, imData.length, options);

                                        // rotate the image to the correct rotation
                                        Matrix mat = new Matrix();
                                        mat.setRotate(90, (float) originalBM.getWidth() / 2, (float) originalBM.getHeight() / 2);
                                        // create new scaled and rotated bitmap
                                        bitmap = Bitmap.createBitmap(originalBM, 0, 0, originalBM.getWidth(), originalBM.getHeight(), mat, true);
                                    }else{
                                        /** if the activity has been re-created
                                         *  then bitmaps have been loaded and stored
                                         *  in the Bundle on saveInstanceState
                                         */
                                        bitmap = bitmapsFromInstanceState.get(index);
                                    }

                                    // programmatically set layout parameters for LinearLayout
                                    LinearLayout.LayoutParams imParams =
                                            new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                                    imParams.gravity = Gravity.CENTER;

                                    imageViews[index] = new ImageView(getApplicationContext());
                                    imageViews[index].setLayoutParams(imParams);
                                    imageViews[index].setImageBitmap(bitmap);
                                    imageViews[index].getAdjustViewBounds();

                                    // map image index to bitmap object
                                    imageToBitmap.put(index, bitmap);

                                    // map image index to coordinate array
                                    imageToCoords.put(index, coordinates);


                                    // action listener for click event (open map fragment in intent)
                                    imageViews[index].setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent mapIntent = new Intent(PhotoFeed.this, Map.class);
                                            // put double[] array of coordinates
                                            mapIntent.putExtra("coordinates", imageToCoords.get(index));
                                            startActivity(mapIntent);
                                        }
                                    });
                                    // action listener for long click event (open DownloadImageService)
                                    imageViews[index].setOnLongClickListener(new View.OnLongClickListener(){
                                        @Override
                                        public boolean onLongClick(View v) {

                                            // set global variable to bitmap object
                                            // more memory safe way than sending bitmap in Intent
                                            ImageTransfer.img = imageToBitmap.get(index);

                                            Intent intent = new Intent(PhotoFeed.this, DownloadImageService.class);
                                            startService(intent);

                                            IntentFilter filter = new IntentFilter(DOWNLOAD_DONE);
                                            // register the receiver with the filter that
                                            // tells the activity that the download is over
                                            registerReceiver(receiver, filter);
                                            // open receiver to get response from service when complete
                                            receiver = new BroadcastReceiver() {
                                                @Override
                                                public void onReceive(Context context, Intent intent) {
                                                    Toast.makeText(getApplicationContext(),
                                                            "Download Complete", Toast.LENGTH_LONG).show();
                                                }
                                            };
                                            try{
                                                unregisterReceiver(receiver);
                                            } catch(IllegalArgumentException e){
                                                // ignore
                                            }
                                            return true;
                                        }
                                    });
                                    innerLayout.addView(imageViews[index]);
                                    setContentView(root);
                                    counter++;

                                } while (images.moveToNext());
                            }
                        });
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item2, menu);
        return true;
    }

    /** when delete button is pressed
     *  clear all the views on the screen
     *  and clear data from database
     * @param item
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        imageDB.clearData();
        root.removeAllViews();
        return true;
    }
}