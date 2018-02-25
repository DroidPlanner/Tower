package co.aerobotics.android.droneshare.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

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
        if (oldVersion == 1) {
            SessionContract.migrateFromV1(db);
        } else {
            Timber.w("Unrecognized database version %d for %s.", oldVersion, SessionContract.DB_NAME);
        }
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
        values.put(SessionContract.SessionData.COLUMN_NAME_CONNECTION_TYPE, connectionType);
        values.put(SessionContract.SessionData.COLUMN_NAME_LABEL, SessionContract.SessionData.getSessionLabel(startTimeInMillis));
        if(tlogLoggingUri != null) {
            values.put(SessionContract.SessionData.COLUMN_NAME_TLOG_LOGGING_URI, tlogLoggingUri.toString());
        }

        return db.insert(SessionContract.SessionData.TABLE_NAME, null, values);
    }

    public void endSessions(long endTimeInMillis, long... rowIds){
        if(rowIds == null || rowIds.length == 0)
            return;

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SessionContract.SessionData.COLUMN_NAME_END_TIME, endTimeInMillis);

        boolean isFirst = true;
        StringBuilder selection = new StringBuilder();
        String[] selectionArgs = new String[rowIds.length];
        int argIndex = 0;
        for(long rowId : rowIds){
            if(!isFirst) {
                selection.append(" OR ");
            }
            else {
                isFirst = false;
            }
            selection.append(SessionContract.SessionData._ID).append(" LIKE ?");

            selectionArgs[argIndex++] = String.valueOf(rowId);
        }

        db.update(SessionContract.SessionData.TABLE_NAME, values, selection.toString(), selectionArgs);
    }

    public SessionContract.SessionData getSessionData(long sessionId){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {SessionContract.SessionData.COLUMN_NAME_START_TIME,
            SessionContract.SessionData.COLUMN_NAME_END_TIME, SessionContract.SessionData.COLUMN_NAME_CONNECTION_TYPE,
            SessionContract.SessionData.COLUMN_NAME_TLOG_LOGGING_URI, SessionContract.SessionData.COLUMN_NAME_LABEL};

        String selection = SessionContract.SessionData._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(sessionId)};

        Cursor cursor = db.query(SessionContract.SessionData.TABLE_NAME, projection, selection, selectionArgs, null,
            null, null);
        SessionContract.SessionData sessionData = null;
        if(cursor.moveToFirst()){
            long startTime = cursor.getLong(cursor.getColumnIndex(SessionContract.SessionData.COLUMN_NAME_START_TIME));
            long endTime = cursor.getLong(cursor.getColumnIndex(SessionContract.SessionData.COLUMN_NAME_END_TIME));
            String connectionTypeLabel = cursor.getString(cursor.getColumnIndex(SessionContract.SessionData.COLUMN_NAME_CONNECTION_TYPE));
            String tlogEncodedUri = cursor.getString(cursor.getColumnIndex(SessionContract.SessionData.COLUMN_NAME_TLOG_LOGGING_URI));
            Uri tlogLoggingUri = Uri.parse(tlogEncodedUri);
            String sessionLabel = cursor.getString(cursor.getColumnIndex(SessionContract.SessionData.COLUMN_NAME_LABEL));

            sessionData = new SessionContract.SessionData(sessionId, startTime, endTime, connectionTypeLabel, tlogLoggingUri, sessionLabel);
        }

        cursor.close();
        return sessionData;
    }

    public long[] getOpenedSessions(){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {SessionContract.SessionData._ID};
        String selection = SessionContract.SessionData.COLUMN_NAME_END_TIME + "IS NULL";

        Cursor cursor = db.query(SessionContract.SessionData.TABLE_NAME, projection, selection, null, null, null, null);
        long[] sessionIds = new long[cursor.getCount()];
        int index = 0;
        for(boolean hasNext = cursor.moveToFirst(); hasNext; hasNext = cursor.moveToNext()){
            sessionIds[index++] = cursor.getLong(cursor.getColumnIndex(SessionContract.SessionData._ID));
        }

        cursor.close();
        return sessionIds;
    }

    public List<SessionContract.SessionData> getCompletedSessions(boolean tlogLogged){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {SessionContract.SessionData._ID, SessionContract.SessionData.COLUMN_NAME_START_TIME,
            SessionContract.SessionData.COLUMN_NAME_END_TIME, SessionContract.SessionData.COLUMN_NAME_CONNECTION_TYPE,
            SessionContract.SessionData.COLUMN_NAME_TLOG_LOGGING_URI, SessionContract.SessionData.COLUMN_NAME_LABEL};

        String selection = SessionContract.SessionData.COLUMN_NAME_END_TIME + " IS NOT NULL";
        if(tlogLogged){
            selection += " AND " + SessionContract.SessionData.COLUMN_NAME_TLOG_LOGGING_URI + " IS NOT NULL";
        }
        String orderBy = SessionContract.SessionData.COLUMN_NAME_START_TIME + " ASC";

        Cursor cursor = db.query(SessionContract.SessionData.TABLE_NAME, projection, selection, null, null, null, orderBy);
        List<SessionContract.SessionData> sessionDataList = new ArrayList<>(cursor.getCount());
        for(boolean hasNext = cursor.moveToFirst(); hasNext; hasNext = cursor.moveToNext()){
            long id = cursor.getLong(cursor.getColumnIndex(SessionContract.SessionData._ID));
            long startTime = cursor.getLong(cursor.getColumnIndex(SessionContract.SessionData.COLUMN_NAME_START_TIME));
            long endTime = cursor.getLong(cursor.getColumnIndex(SessionContract.SessionData.COLUMN_NAME_END_TIME));
            String connectionTypeLabel = cursor.getString(cursor.getColumnIndex(SessionContract.SessionData.COLUMN_NAME_CONNECTION_TYPE));
            String tlogEncodedUri = cursor.getString(cursor.getColumnIndex(SessionContract.SessionData.COLUMN_NAME_TLOG_LOGGING_URI));
            Uri tlogLoggingUri = Uri.parse(tlogEncodedUri);
            String sessionLabel = cursor.getString(cursor.getColumnIndex(SessionContract.SessionData.COLUMN_NAME_LABEL));

            sessionDataList.add(new SessionContract.SessionData(id, startTime, endTime, connectionTypeLabel, tlogLoggingUri, sessionLabel));
        }

        cursor.close();
        return sessionDataList;
    }

    public void removeSessionData(long id) {
        SQLiteDatabase db = getWritableDatabase();

        String whereClause = SessionContract.SessionData._ID + " LIKE ?";
        String[] whereArgs = {String.valueOf(id)};

        db.delete(SessionContract.SessionData.TABLE_NAME, whereClause, whereArgs);
    }

    public void renameSession(long id, String label) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SessionContract.SessionData.COLUMN_NAME_LABEL, label);

        String whereClause = SessionContract.SessionData._ID + " LIKE ?";
        String[] whereArgs = {String.valueOf(id)};

        db.update(SessionContract.SessionData.TABLE_NAME, values, whereClause, whereArgs);
    }

    public void cleanupOpenedSessions(long endTimeInMillis){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SessionContract.SessionData.COLUMN_NAME_END_TIME, endTimeInMillis);

        String selection = SessionContract.SessionData.COLUMN_NAME_END_TIME + " IS NULL";
        db.update(SessionContract.SessionData.TABLE_NAME, values, selection, null);
    }
}
