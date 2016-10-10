package org.droidplanner.android.droneshare.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Defines the schema for the Session database.
 */
public final class SessionContract {

    static final String DB_NAME = "session";
    static final int DB_VERSION = 2;

    //Private constructor to prevent instantiation.
    private SessionContract(){}

    static String getSqlCreateEntries(){
        return SessionData.SQL_CREATE_ENTRIES;
    }

    static String getSqlDeleteEntries(){
        return SessionData.SQL_DELETE_ENTRIES;
    }

    static void migrateFromV1(SQLiteDatabase db) {
        SessionData.migrateFromV1(db);
    }

    /**
     * Defines the schema for the SessionData table.
     */
    public static final class SessionData implements BaseColumns {
        private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS",Locale.US);

        static final String TABLE_NAME = "session_data";

        static final String COLUMN_NAME_START_TIME ="start_time";
        static final String COLUMN_NAME_END_TIME = "end_time";
        static final String COLUMN_NAME_CONNECTION_TYPE = "connection_type";
        static final String COLUMN_NAME_TLOG_LOGGING_URI = "tlog_logging_uri";
        static final String COLUMN_NAME_LABEL = "session_label";

        static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME_START_TIME + " INTEGER NOT NULL," +
                COLUMN_NAME_END_TIME + " INTEGER," +
                COLUMN_NAME_CONNECTION_TYPE + " TEXT NOT NULL," +
                COLUMN_NAME_TLOG_LOGGING_URI + " TEXT," +
                COLUMN_NAME_LABEL + " TEXT NOT NULL" +
                " )";

        static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        public final long id;
        public final long startTime;
        public final long endTime;
        public final String connectionTypeLabel;
        public final Uri tlogLoggingUri;
        public final String label;

        SessionData(long id, long startTime, long endTime, String connectionTypeLabel, Uri tlogLoggingUri, String label) {
            this.id = id;
            this.startTime = startTime;
            this.endTime = endTime;
            this.connectionTypeLabel = connectionTypeLabel;
            this.tlogLoggingUri = tlogLoggingUri;
            this.label = label;
        }

        private SessionData(long id, long startTime, long endTime, String connectionTypeLabel, Uri tlogLoggingUri){
            this(id, startTime, endTime, connectionTypeLabel, tlogLoggingUri, getSessionLabel(startTime));
        }

        static String getSessionLabel(long startTime) {
            return dateFormatter.format(new Date(startTime));
        }

        private static void migrateFromV1(SQLiteDatabase db) {
            // Get all the session data from the v1 table.
            List<SessionData> v1Data = getV1SessionData(db);

            // Delete the v1 table.
            db.execSQL(SQL_DELETE_ENTRIES);

            // Create the new table.
            db.execSQL(SQL_CREATE_ENTRIES);

            // Transfer the data to the new table.
            for (SessionData data : v1Data) {
                insertV2SessionData(db, data.startTime, data.endTime, data.connectionTypeLabel,
                    data.tlogLoggingUri, data.label);
            }
        }

        private static void insertV2SessionData(SQLiteDatabase db, long startTime, long endTime,
                                                String connectionTypeLabel, Uri tlogLoggingUri, String label) {
            ContentValues values = new ContentValues();
            values.put(SessionContract.SessionData.COLUMN_NAME_START_TIME, startTime);
            values.put(SessionData.COLUMN_NAME_CONNECTION_TYPE, connectionTypeLabel);
            values.put(SessionData.COLUMN_NAME_LABEL, label);
            values.put(SessionData.COLUMN_NAME_END_TIME, endTime);
            if(tlogLoggingUri != null) {
                values.put(SessionData.COLUMN_NAME_TLOG_LOGGING_URI, tlogLoggingUri.toString());
            }

            db.insert(SessionData.TABLE_NAME, null, values);
        }

        private static List<SessionData> getV1SessionData(SQLiteDatabase db) {
            String[] projection = {SessionData._ID, SessionData.COLUMN_NAME_START_TIME,
                SessionData.COLUMN_NAME_END_TIME, SessionData.COLUMN_NAME_CONNECTION_TYPE,
                SessionData.COLUMN_NAME_TLOG_LOGGING_URI};

            String orderBy = SessionData.COLUMN_NAME_START_TIME + " ASC";

            Cursor cursor = db.query(SessionData.TABLE_NAME, projection, null, null, null, null, orderBy);
            List<SessionData> sessionDataList = new ArrayList<>(cursor.getCount());
            for(boolean hasNext = cursor.moveToFirst(); hasNext; hasNext = cursor.moveToNext()){
                long id = cursor.getLong(cursor.getColumnIndex(SessionData._ID));
                long startTime = cursor.getLong(cursor.getColumnIndex(SessionData.COLUMN_NAME_START_TIME));
                long endTime = cursor.getLong(cursor.getColumnIndex(SessionData.COLUMN_NAME_END_TIME));
                String connectionTypeLabel = cursor.getString(cursor.getColumnIndex(SessionData.COLUMN_NAME_CONNECTION_TYPE));
                String tlogEncodedUri = cursor.getString(cursor.getColumnIndex(SessionData.COLUMN_NAME_TLOG_LOGGING_URI));
                Uri tlogLoggingUri = Uri.parse(tlogEncodedUri);

                sessionDataList.add(new SessionData(id, startTime, endTime, connectionTypeLabel, tlogLoggingUri));
            }

            cursor.close();
            return sessionDataList;
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

            if (tlogLoggingUri != null) {
                return tlogLoggingUri.equals(that.tlogLoggingUri);
            } else {
                return that.tlogLoggingUri == null
                    && startTime == that.startTime
                    && connectionTypeLabel.equals(that.connectionTypeLabel);
            }
        }

        @Override
        public int hashCode() {
            if (tlogLoggingUri != null) {
                return tlogLoggingUri.hashCode();
            } else {
                int result = (int) (id ^ (id >>> 32));
                result = 31 * result + (int) (startTime ^ (startTime >>> 32));
                result = 31 * result + (int) (endTime ^ (endTime >>> 32));
                result = 31 * result + connectionTypeLabel.hashCode();
                result = 31 * result + label.hashCode();
                return result;
            }
        }

        @Override
        public String toString() {
            return "SessionData{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", connectionTypeLabel='" + connectionTypeLabel + '\'' +
                ", tlogLoggingUri=" + tlogLoggingUri +
                ", label='" + label + '\'' +
                '}';
        }
    }
}
