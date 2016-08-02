package org.droidplanner.android.droneshare.data;

import android.provider.BaseColumns;

/**
 * Defines the schema for the DroneShare database
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
public final class DroneShareContract {

    static final String DB_NAME = "droneshare";
    static final int DB_VERSION = 2;

    private DroneShareContract(){}

    static String[] getSQLCreateEntries(){
        return new String[]{
            UploadData.SQL_CREATE_ENTRIES,
        };
    }

    static String[] getSQLDeleteEntries(){
        return new String[]{
            UploadData.SQL_DELETE_ENTRIES,
        };
    }

    static final class UploadData implements BaseColumns {
        static final String TABLE_NAME = "upload_data";

        static final String COL_SESSION_ID = "session_id";
        static final String COL_DSHARE_USER = "drone_share_username";
        static final String COL_DATA_UPLOAD_TIME = "data_upload_time";

        static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COL_DSHARE_USER + " TEXT NOT NULL," +
                COL_SESSION_ID + " INTEGER NOT NULL," +
                COL_DATA_UPLOAD_TIME + " INTEGER" +
                " )";

        static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
