package org.droidplanner.android.droneshare.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.support.v4.util.Pair
import org.droidplanner.android.droneshare.data.DroneShareContract.UploadData
import org.droidplanner.android.droneshare.data.SessionContract.SessionData
import timber.log.Timber
import java.util.*

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class DroneShareDB(context: Context) :
        SQLiteOpenHelper(context, DroneShareContract.DB_NAME, null, DroneShareContract.DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
          Timber.i("Creating ${DroneShareContract.DB_NAME} database.")
        val sqlCreateEntries = DroneShareContract.getSQLCreateEntries()
        for(sqlCreateEntry in sqlCreateEntries) {
            db.execSQL(sqlCreateEntry)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Timber.i("Upgrading ${DroneShareContract.DB_NAME} database from version $oldVersion to version $newVersion")
        val sqlDelEntries = DroneShareContract.getSQLDeleteEntries()
        for(sqlDelEntry in sqlDelEntries) {
            db.execSQL(sqlDelEntry)
        }
        onCreate(db)
    }

    fun queueDataUploadEntry(username: String, sessionId: Long){
        val db = getWritableDatabase()

        val values = ContentValues().apply{
            put(UploadData.COL_DSHARE_USER, username)
            put(UploadData.COL_SESSION_ID, sessionId)
        }

        db.insert(UploadData.TABLE_NAME, null, values)
    }

    /**
     * Returns the list of data for the given user that have yet to be uploaded to droneshare
     */
    fun getDataToUpload(username: String): List<Pair<Long, Uri>> {
        val db = getReadableDatabase()

        val query = "SELECT ${UploadData._ID}, ${SessionData.COLUMN_NAME_TLOG_LOGGING_URI} " +
                "FROM ${UploadData.TABLE_NAME} JOIN ${SessionData.TABLE_NAME} " +
                "ON ${UploadData.COL_SESSION_ID} = ${SessionData._ID} " +
                "WHERE ${UploadData.COL_DATA_UPLOAD_TIME} IS NULL " +
                "AND ${SessionData.COLUMN_NAME_END_TIME} IS NOT NULL " +
                "AND ${UploadData.COL_DSHARE_USER} LIKE ? " +
                "ORDER BY ${UploadData._ID} ASC"
        val selectionArgs = arrayOf(username)

        val cursor = db.rawQuery(query, selectionArgs)
        val result = ArrayList<Pair<Long, Uri>>(cursor.count)
        if(cursor.moveToFirst()){
            do {
                val uploadId = cursor.getLong(cursor.getColumnIndex(UploadData._ID))
                val dataUri = Uri.parse(cursor.getString(cursor.getColumnIndex(SessionData.COLUMN_NAME_TLOG_LOGGING_URI)))
                result.add(Pair(uploadId, dataUri))
            } while(cursor.moveToNext())
        }

        cursor.close()
        return result
    }

    fun commitUploadedData(uploadId: Long, uploadTimeInMillis: Long){
        val db = getWritableDatabase()

        val values = ContentValues().apply {
            put(UploadData.COL_DATA_UPLOAD_TIME, uploadTimeInMillis)
        }

        val selection = "${UploadData._ID} LIKE ?"
        val selectionArgs = arrayOf(uploadId.toString())

        db.update(UploadData.TABLE_NAME, values, selection, selectionArgs)
    }

}