package org.droidplanner.android.maps.providers.google_map.tiles.offline.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OfflineDatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = OfflineDatabaseHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    public static final int DATABASE_VERSION = 1;

    // Table name(s)
    public static final String TABLE_DATA = "data";
    public static final String TABLE_RESOURCES = "resources";

    // Table Fields
    public static final String FIELD_DATA_ID = "id";
    public static final String FIELD_DATA_VALUE = "value";

    public static final String FIELD_RESOURCES_ID = "id";
    public static final String FIELD_RESOURCES_URL = "url";
    public static final String FIELD_RESOURCES_STATUS = "status";

    /**
     * Constructor
     *
     * @param context Context
     */
    public OfflineDatabaseHandler(Context context, String dbName) {
        super(context, dbName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate() called... Setting up application's database.");
        // Create The table(s)
        String data = "CREATE TABLE " + TABLE_DATA + " (" + FIELD_DATA_ID + " INTEGER PRIMARY KEY, " + FIELD_DATA_VALUE + " BLOB);";
        String resources = "CREATE TABLE " + TABLE_RESOURCES + " (" + FIELD_RESOURCES_URL + " TEXT UNIQUE, " + FIELD_RESOURCES_STATUS + " TEXT, " + FIELD_RESOURCES_ID + " INTEGER REFERENCES data);";

        db.execSQL("PRAGMA foreign_keys=ON;");
        db.beginTransaction();

        try {
            db.execSQL(data);
            db.execSQL(resources);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Error creating databases", e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("PRAGMA foreign_keys=OFF;");
        db.execSQL("drop table if exists " + TABLE_DATA);
        db.execSQL("drop table if exists " + TABLE_RESOURCES);
        onCreate(db);
    }

    public byte[] dataForURL(String url) {
        return sqliteDataForURL(url);
    }

    public byte[] sqliteDataForURL(String url) {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + OfflineDatabaseHandler.FIELD_DATA_VALUE + " FROM " + OfflineDatabaseHandler.TABLE_DATA +
                " WHERE " + OfflineDatabaseHandler.FIELD_DATA_ID +
                "= (SELECT " + OfflineDatabaseHandler.FIELD_RESOURCES_ID + " from " +
                OfflineDatabaseHandler.TABLE_RESOURCES + " where " + OfflineDatabaseHandler.FIELD_RESOURCES_URL +
                " = '" + url + "');";

        Cursor cursor = db.rawQuery(query, null);

        byte[] blob = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                blob = cursor.getBlob(cursor.getColumnIndex(OfflineDatabaseHandler.FIELD_DATA_VALUE));
            }
            cursor.close();
        }
//        db.close();
        return blob;
    }
}
