package com1032.cw2.ms01288.ms01288_assignment2;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Arrays;


/**
 * Created by marcstevens on 26/04/2017.
 */

public class ImageDB extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String IMAGE_TABLE_NAME = "images";

    public ImageDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    /** called on onCreate
     *
     * @param db
     */
    private void createTable(SQLiteDatabase db) {
        /** immediately create table when imageDB instance created
         *  this table holds the byte array as well as latitude and longitude
         *  coordinates */
        String createSQL = "CREATE TABLE " + IMAGE_TABLE_NAME + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "IMAGEDATA BLOB, " +
                "LAT REAL, " +
                "LON REAL);";
        db.execSQL(createSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // remove then re-create image table
        String dropSQL = "DROP TABLE IF EXISTS " + IMAGE_TABLE_NAME + ";";
        db.execSQL(dropSQL);

        createTable(db);
    }

    public void clearData(){

        SQLiteDatabase db = this.getWritableDatabase();

        String dropSQL = "DROP TABLE IF EXISTS " + IMAGE_TABLE_NAME + ";";
        db.execSQL(dropSQL);

        createTable(db);
    }

    /** Method used to insert a row of data
     * to the database
     * @param imageData
     * @param lat
     * @param lon
     */
    public void insertData(byte[] imageData, double lat, double lon){

        SQLiteDatabase dbImages = this.getWritableDatabase();

        /** add the row of data in a ContentValues
         * to prevent imageData byte array from
         * being subject to toString()
         */
        dbImages.insert("images", null, createContentValues(imageData, lat, lon));

        // close database
        this.close();
    }

    /** return a ContentValues object
     * of all the data items to insert
     * @param image
     * @param lat
     * @param lon
     * @return ContentValues
     */
    private ContentValues createContentValues(byte[] image, double lat, double lon) {

        ContentValues cv = new ContentValues();

        cv.put("IMAGEDATA", image);
        cv.put("LAT", lat);
        cv.put("LON", lon);

        return cv;
    }


}
