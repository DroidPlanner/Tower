package org.droidplanner.android.droneshare.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines the schema for the Session database.
 */
public final class SessionContract {

    static final String DB_NAME = "session";
    static final int DB_VERSION = 1;

    //Private constructor to prevent instantiation.
    private SessionContract(){}

    static String getSqlCreateEntries(){
        return SessionData.SQL_CREATE_ENTRIES;
    }

    static String getSqlDeleteEntries(){
        return SessionData.SQL_DELETE_ENTRIES;
    }

    /**
     * Defines the schema for the SessionData table.
     */
    public static final class SessionData implements BaseColumns {
        static final String TABLE_NAME = "session_data";

        static final String COLUMN_NAME_START_TIME ="start_time";
        static final String COLUMN_NAME_END_TIME = "end_time";
        static final String COLUMN_NAME_CONNECTION_TYPE = "connection_type";
        static final String COLUMN_NAME_TLOG_LOGGING_URI = "tlog_logging_uri";

        static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_START_TIME + " INTEGER NOT NULL," +
                        COLUMN_NAME_END_TIME + " INTEGER," +
                        COLUMN_NAME_CONNECTION_TYPE + " TEXT NOT NULL," +
                        COLUMN_NAME_TLOG_LOGGING_URI + " TEXT" +
                        " )";

        static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        public final long id;
        public final long startTime;
        public final long endTime;
        public final String connectionTypeLabel;
        public final Uri tlogLoggingUri;

        SessionData(long id, long startTime, long endTime, String connectionTypeLabel, Uri tlogLoggingUri) {
            this.id = id;
            this.startTime = startTime;
            this.endTime = endTime;
            this.connectionTypeLabel = connectionTypeLabel;
            this.tlogLoggingUri = tlogLoggingUri;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SessionData)) {
                return false;
            }

            SessionData that = (SessionData) o;

            return tlogLoggingUri != null ? tlogLoggingUri.equals(that.tlogLoggingUri) : that.tlogLoggingUri == null;

        }

        @Override
        public int hashCode() {
            return tlogLoggingUri != null ? tlogLoggingUri.hashCode() : 0;
        }
    }
}
