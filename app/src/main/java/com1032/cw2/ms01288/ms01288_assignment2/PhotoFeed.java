package com1032.cw2.ms01288.ms01288_assignment2;

import android.content.Intent;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;

import static com1032.cw2.ms01288.ms01288_assignment2.MainActivity.imageDB;

public class PhotoFeed extends AppCompatActivity {

    private String[] columnNames = {"TIMESTAMP","IMAGEDATA","LAT","LON"};
    private Cursor images = null;
    LinearLayout root;
    ScrollView scrollView;
    LinearLayout innerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_feed);

        SQLiteDatabase db = imageDB.getReadableDatabase();
        images = db.query("images", columnNames, null, null, null, null, null);

        root = (LinearLayout) findViewById(R.id.photoFeedLayout);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        innerLayout = (LinearLayout) findViewById(R.id.innerLayout);

        runThread(); // start a UI thread

    }
    private void runThread() {
        new Thread() {
            public void run() {
                if(images.moveToFirst()){
                    try {
                        runOnUiThread(new Runnable() {
                            ImageView[] imageViews = new ImageView[12];
                            int index = 0;

                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void run() {
                                do {
                                    byte[] imData = images.getBlob(images.getColumnIndex("IMAGEDATA"));
                                    final double lat = images.getDouble(images.getColumnIndex("LAT"));
                                    final double lon = images.getDouble(images.getColumnIndex("LON"));

                                    Bitmap bm = BitmapFactory.decodeByteArray(imData, 0, imData.length);

                                    LinearLayout.LayoutParams imParams =
                                    new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                                    imParams.gravity = Gravity.TOP;

                                    Matrix mat = new Matrix();

                                    mat.setRotate(90, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);

                                    Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mat, true);

                                    imageViews[index] = new ImageView(getApplicationContext());
                                    imageViews[index].setLayoutParams(imParams);
                                    imageViews[index].setImageBitmap(bm1);
                                    imageViews[index].getAdjustViewBounds();
                                    imageViews[index].setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent mapIntent = new Intent(PhotoFeed.this, Map.class);
                                            mapIntent.putExtra("Latitude", lat);
                                            mapIntent.putExtra("Longitude", lon);
                                            startActivity(mapIntent);
                                        }
                                    });

                                    innerLayout.addView(imageViews[index]);
                                    setContentView(root);

                                    index++;

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

}
