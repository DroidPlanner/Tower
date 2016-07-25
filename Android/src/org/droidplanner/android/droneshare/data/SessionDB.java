package org.droidplanner.android.droneshare.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;

import org.droidplanner.android.droneshare.data.SessionContract.SessionData;

import timber.log.Timber;

/**
 * Created by fhuya on 12/30/14.
 */
public class SessionDB extends SQLiteOpenHelper {

    public SessionDB(Context context) {
        super(context, SessionContract.DB_NAME, null, SessionContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Timber.i("Creating session database.");
        db.execSQL(SessionContract.getSqlCreateEntries());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SessionContract.getSqlDeleteEntries());
        onCreate(db);
    }

    /**
     * Return a unique id for the session that can be used to close it
     * @param startTimeInMillis
     * @param connectionType
     */
    public long startSession(long startTimeInMillis, String connectionType, @Nullable Uri tlogLoggingUri){
        //Get the data repository in write mode.
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SessionContract.SessionData.COLUMN_NAME_START_TIME, startTimeInMillis);
        values.put(SessionData.COLUMN_NAME_CONNECTION_TYPE, connectionType);
        if(tlogLoggingUri != null) {
            values.put(SessionData.COLUMN_NAME_TLOG_LOGGING_URI, tlogLoggingUri.toString());
        }

        return db.insert(SessionData.TABLE_NAME, null, values);
    }

    public void endSession(long rowId, long endTimeInMillis){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SessionData.COLUMN_NAME_END_TIME, endTimeInMillis);

        String selection = SessionData._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(rowId)};

        db.update(SessionData.TABLE_NAME, values, selection, selectionArgs);
    }

    public void cleanupOpenedSessions(long endTimeInMillis){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SessionData.COLUMN_NAME_END_TIME, endTimeInMillis);

        String selection = SessionData.COLUMN_NAME_END_TIME + " IS NULL";
        db.update(SessionData.TABLE_NAME, values, selection, null);
    }
}
