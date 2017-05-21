package com1032.cw2.ms01288.ms01288_assignment2;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class DownloadImageService extends IntentService {

    public static final String DOWNLOAD_DONE = "DOWNLOAD_DONE";

    public DownloadImageService() {
        super(DownloadImageService.class.getName());
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // download image here
        Bitmap bm = ImageTransfer.img;
        downloadFile(bm);

        Intent i = new Intent(DOWNLOAD_DONE);
        this.sendBroadcast(i);
    }
    public void downloadFile(Bitmap bm){

        //get root in external storage
        String root = Environment.getExternalStorageDirectory().toString();
        //make directory to save File to
        File myDir = new File(root + "/app_images");
        myDir.mkdirs();

        //generate random number to append to image name
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fName = "Image-" + n + ".jpg";

        File file = new File(myDir, fName);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        /** make image file immediately visible
         * inside the phone's gallery using ACTION_MEDIA_SCANNER_SCAN_FILE
        */
        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri fileContentUri = Uri.fromFile(file);
        mediaScannerIntent.setData(fileContentUri);
        this.sendBroadcast(mediaScannerIntent);
    }
}
