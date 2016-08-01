package org.droidplanner.android.droneshare.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines the schema for the DroneShare database
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
public final class DroneShareContract {

    static final String DB_NAME = "droneshare";
    static final int DB_VERSION = 1;

    private DroneShareContract(){}

    static String[] getSQLCreateEntries(){
        return new String[]{
            UploadData.SQL_CREATE_ENTRIES,
            TLogMetadata.SQL_CREATE_ENTRIES
        };
    }

    static String[] getSQLDeleteEntries(){
        return new String[]{
            UploadData.SQL_DELETE_ENTRIES,
            TLogMetadata.SQL_DELETE_ENTRIES
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

    public static final class TLogMetadata implements BaseColumns {
        static final String TABLE_NAME = "tlog_metadata";

        static final String COL_SESSION_ID = "session_id";
        static final String COL_TLOG_URI = "tlog_uri";
        static final String COL_TLOG_ALL_EVENTS_COUNT = "all_events_count";
        static final String COL_TLOG_POSITION_EVENTS_COUNT = "position_events_count";

        static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COL_TLOG_URI + " TEXT NOT NULL," +
                COL_SESSION_ID + " INTEGER, " +
                COL_TLOG_ALL_EVENTS_COUNT + " INTEGER NOT NULL, " +
                COL_TLOG_POSITION_EVENTS_COUNT + " INTEGER NOT NULL " +
                ")";

        static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public final long id;
        public final Uri tlogUri;
        public final long sessionId;
        public final int allEventsCount;
        public final int positionEventsCount;

        public TLogMetadata(long id, Uri tlogUri, int allEventsCount, int positionEventsCount) {
            this(id, tlogUri, -1, allEventsCount, positionEventsCount);
        }

        public TLogMetadata(long id, Uri tlogUri, long sessionId, int allEventsCount, int positionEventsCount) {
            this.id = id;
            this.tlogUri = tlogUri;
            this.sessionId = sessionId;
            this.allEventsCount = allEventsCount;
            this.positionEventsCount = positionEventsCount;
        }
    }
}
