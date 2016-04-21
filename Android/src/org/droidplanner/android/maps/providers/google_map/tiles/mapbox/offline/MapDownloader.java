package org.droidplanner.android.maps.providers.google_map.tiles.mapbox.offline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.droidplanner.android.data.DatabaseState;
import org.droidplanner.android.maps.providers.google_map.tiles.offline.MapDownloaderListener;
import org.droidplanner.android.maps.providers.google_map.tiles.offline.db.OfflineDatabaseHandler;
import org.droidplanner.android.utils.NetworkUtils;
import org.droidplanner.android.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

public class MapDownloader {

    /**
     * The possible states of the offline map downloader.
     */
    public enum OfflineMapDownloaderState {
        /**
         * An offline map download job is in progress.
         */
        RUNNING,
        /**
         * An offline map download job is being canceled.
         */
        CANCELLING,
        /**
         * The offline map downloader is ready to begin a new offline map download job.
         */
        AVAILABLE
    }

    private OfflineMapDownloaderState state;
    private final AtomicInteger totalFilesWritten = new AtomicInteger(0);
    private final AtomicInteger totalFilesExpectedToWrite = new AtomicInteger(0);

    private final Context context;
    private ExecutorService downloadsScheduler;
    private final ArrayList<MapDownloaderListener> listeners = new ArrayList<>();

    public MapDownloader(Context context) {
        this.context = context;
        setupDownloadScheduler();

        this.state = OfflineMapDownloaderState.AVAILABLE;
    }

    public OfflineMapDownloaderState getState() {
        return state;
    }

    public boolean addMapDownloaderListener(MapDownloaderListener listener) {
        if(listener != null) {
            listener.stateChanged(this.state);
            return listeners.add(listener);
        }
        return false;
    }

    public boolean removeMapDownloaderListener(MapDownloaderListener listener) {
        return listeners.remove(listener);
    }

    public void cancelDownload() {
        if (state == OfflineMapDownloaderState.RUNNING) {
            this.state = OfflineMapDownloaderState.CANCELLING;
            notifyDelegateOfStateChange();
        }

        setupDownloadScheduler();

        if (state == OfflineMapDownloaderState.CANCELLING) {
            this.state = OfflineMapDownloaderState.AVAILABLE;
            notifyDelegateOfStateChange();
        }
    }

    private void setupDownloadScheduler() {
        if (downloadsScheduler != null) {
            downloadsScheduler.shutdownNow();
        }

        final int processorsCount = (int) (Runtime.getRuntime().availableProcessors() * 1.5f);
        Timber.v("Using " + processorsCount + " processors.");
        downloadsScheduler = Executors.newFixedThreadPool(processorsCount);
    }

/*
    Delegate Notifications
*/
    public void notifyDelegateOfStateChange() {
        for (MapDownloaderListener listener : listeners) {
            listener.stateChanged(this.state);
        }
    }

    public void notifyDelegateOfInitialCount(int totalFilesExpectedToWrite) {
        for (MapDownloaderListener listener : listeners) {
            listener.initialCountOfFiles(totalFilesExpectedToWrite);
        }
    }

    public void notifyDelegateOfProgress(int totalFilesWritten, int totalFilesExpectedToWrite) {
        for (MapDownloaderListener listener : listeners) {
            listener.progressUpdate(totalFilesWritten, totalFilesExpectedToWrite);
        }
    }

    public void notifyDelegateOfNetworkConnectivityError(Throwable error) {
        for (MapDownloaderListener listener : listeners) {
            listener.networkConnectivityError(error);
        }
    }

    public void notifyDelegateOfSqliteError(Throwable error) {
        for (MapDownloaderListener listener : listeners) {
            listener.sqlLiteError(error);
        }
    }

    public void notifyDelegateOfHTTPStatusError(int status, String url) {
        for (MapDownloaderListener listener : listeners) {
            listener.httpStatusError(status, url);
        }
    }

    public void notifyDelegateOfCompletionWithOfflineMapDatabase() {
        for (MapDownloaderListener listener : listeners) {
            listener.completionOfOfflineDatabaseMap();
        }
    }

    private void startDownloading(final String mapId) {

        // Get the actual URLs
        ArrayList<String> urls = sqliteReadArrayOfOfflineMapURLsToBeDownloadLimit(mapId, -1);
        this.totalFilesExpectedToWrite.set(urls.size());
        this.totalFilesWritten.set(0);

        notifyDelegateOfInitialCount(totalFilesExpectedToWrite.get());

        Timber.d(String.format(Locale.US, "number of urls to download = %d", urls.size()));
        if (this.totalFilesExpectedToWrite.get() == 0) {
            finishUpDownloadProcess();
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            Timber.e("Network is not available.");
            notifyDelegateOfNetworkConnectivityError(new IllegalStateException("Network is not available"));
            return;
        }

        final CountDownLatch downloadsTracker = new CountDownLatch(this.totalFilesExpectedToWrite.get());
        for (final String url : urls) {
            downloadsScheduler.execute(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection conn = null;
                    try {
                        conn = NetworkUtils.getHttpURLConnection(new URL(url));
                        Timber.d("URL to download = " + conn.getURL().toString());
                        conn.setConnectTimeout(60000);
                        conn.connect();
                        int rc = conn.getResponseCode();
                        if (rc != HttpURLConnection.HTTP_OK) {
                            String msg = String.format(Locale.US, "HTTP Error connection.  Response Code = %d for url = %s", rc, conn.getURL().toString());
                            Timber.w(msg);
                            notifyDelegateOfHTTPStatusError(rc, url);
                            throw new IOException(msg);
                        }

                        ByteArrayOutputStream bais = new ByteArrayOutputStream();
                        InputStream is = null;
                        try {
                            is = conn.getInputStream();
                            // Read 4K at a time
                            byte[] byteChunk = new byte[4096];
                            int n;

                            while ((n = is.read(byteChunk)) > 0) {
                                bais.write(byteChunk, 0, n);
                            }
                        } catch (IOException e) {
                            Timber.e(e, String.format(Locale.US, "Failed while reading bytes from %s: %s", conn
                                    .getURL().toString(), e.getMessage()));
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                            conn.disconnect();
                        }
                        sqliteSaveDownloadedData(mapId, bais.toByteArray(), url);
                    } catch (IOException e) {
                        Timber.e(e, "Error occurred while retrieving map data.");
                    } finally {
                        downloadsTracker.countDown();

                        if (conn != null) {
                            conn.disconnect();
                        }
                    }

                }
            });
        }

        downloadsScheduler.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    downloadsTracker.await();
                } catch (InterruptedException e) {
                    Timber.e(e, "Error while waiting for downloads to complete.");
                } finally {
                    finishUpDownloadProcess();
                }
            }
        });
    }

/*
    Implementation: sqlite stuff
*/

    public void sqliteSaveDownloadedData(String mapId, byte[] data, String url) {
        if (Utils.runningOnMainThread()) {
            Timber.w("trying to run sqliteSaveDownloadedData() on main thread. Return.");
            return;
        }

        // Bail out if the state has changed to canceling, suspended, or available
        if (this.state != OfflineMapDownloaderState.RUNNING) {
            Timber.w("sqliteSaveDownloadedData() is not in a Running state so bailing.  State = " + this.state);
            return;
        }

        // Open the database read-write and multi-threaded. The slightly obscure c-style variable names here and below are
        // used to stay consistent with the sqlite documentaion.
        // Continue by inserting an image blob into the data table
        SQLiteDatabase db = DatabaseState.getOfflineDatabaseHandlerForMapId(context, mapId).getWritableDatabase();
        try {
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(OfflineDatabaseHandler.FIELD_DATA_VALUE, data);
            db.insert(OfflineDatabaseHandler.TABLE_DATA, null, values);

            db.execSQL(String.format(Locale.US, "UPDATE %s SET %s=200, %s=last_insert_rowid() WHERE %s='%s';", OfflineDatabaseHandler.TABLE_RESOURCES, OfflineDatabaseHandler.FIELD_RESOURCES_STATUS, OfflineDatabaseHandler.FIELD_RESOURCES_ID, OfflineDatabaseHandler.FIELD_RESOURCES_URL, url));
            db.setTransactionSuccessful();
            db.endTransaction();
//        db.close();
        }catch(IllegalStateException e){
            Timber.e(e,"Error while saving downloader data to the database.");
        }

        // Update the progress
        notifyDelegateOfProgress(this.totalFilesWritten.incrementAndGet(), this.totalFilesExpectedToWrite.get());
        Timber.d("totalFilesWritten = " + this.totalFilesWritten + "; totalFilesExpectedToWrite = " + this
                .totalFilesExpectedToWrite.get());
    }

    private void finishUpDownloadProcess() {
        if (this.state == OfflineMapDownloaderState.RUNNING) {
            Timber.i("Just finished downloading all materials.  Persist the OfflineMapDatabase, change the state, and call it a day.");
            // This is what to do when we've downloaded all the files
            notifyDelegateOfCompletionWithOfflineMapDatabase();
            this.state = OfflineMapDownloaderState.AVAILABLE;
            notifyDelegateOfStateChange();
        }
    }

    public ArrayList<String> sqliteReadArrayOfOfflineMapURLsToBeDownloadLimit(String mapId, int limit) {
        ArrayList<String> results = new ArrayList<String>();
        if (Utils.runningOnMainThread()) {
            Timber.w("Attempting to run sqliteReadArrayOfOfflineMapURLsToBeDownloadLimit() on main thread.  Returning.");
            return results;
        }

        // Read up to limit undownloaded urls from the offline map database
        String query = String.format(Locale.US, "SELECT %s FROM %s WHERE %s IS NULL", OfflineDatabaseHandler.FIELD_RESOURCES_URL, OfflineDatabaseHandler.TABLE_RESOURCES, OfflineDatabaseHandler.FIELD_RESOURCES_STATUS);
        if (limit > 0) {
            query = query + String.format(Locale.US, " LIMIT %d", limit);
        }
        query = query + ";";

        // Open the database
        SQLiteDatabase db = DatabaseState.getOfflineDatabaseHandlerForMapId(context, mapId).getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    results.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
//        db.close();

        return results;
    }


    public boolean sqliteCreateDatabaseUsingMetadata(String mapId, List<String> urlStrings) {
        if (Utils.runningOnMainThread()) {
            Timber.w("sqliteCreateDatabaseUsingMetadata() running on main thread.  Returning.");
            return false;
        }

        final OfflineDatabaseHandler dbHandler = DatabaseState.getOfflineDatabaseHandlerForMapId(context, mapId);
        if(dbHandler == null)
            return false;

        // Build a query to populate the database (map metadata and list of map resource urls)
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        db.beginTransaction();

        for (String url : urlStrings) {
            ContentValues cv = new ContentValues();
            cv.put(OfflineDatabaseHandler.FIELD_RESOURCES_URL, url);
            db.insertWithOnConflict(OfflineDatabaseHandler.TABLE_RESOURCES, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
//        db.close();

        return true;
    }

    private boolean deleteIncompleteDownloads(String mapId){
        if(TextUtils.isEmpty(mapId))
            return false;

        if(Utils.runningOnMainThread()){
            Timber.w("This call should not be made on the main thread.");
            return false;
        }

        final OfflineDatabaseHandler dbHandler = DatabaseState.getOfflineDatabaseHandlerForMapId(context, mapId);
        if(dbHandler == null)
            return false;

        final SQLiteDatabase db = dbHandler.getWritableDatabase();
        final int deletedCount = db.delete(OfflineDatabaseHandler.TABLE_RESOURCES,
                "status IS NULL OR TRIM(status) = ''", null);
        Timber.d("Deleted %d rows", deletedCount);
        return true;
    }

/*
    API: Begin an offline map download
*/

    /**
     * Starting the Whole Download Process
     *
     * @param urls Map urls
     */
    public void startDownloadProcess(final String mapId, final List<String> urls) {
        if (state != OfflineMapDownloaderState.AVAILABLE) {
            Timber.w("state doesn't equal AVAILABLE so return.  state = " + state);
            return;
        }

        // Start a download job to retrieve all the resources needed for using the specified map offline
        this.state = OfflineMapDownloaderState.RUNNING;
        notifyDelegateOfStateChange();

        downloadsScheduler.execute(new Runnable() {
            @Override
            public void run() {
                deleteIncompleteDownloads(mapId);

                // Do database creation / io on background thread
                if (!sqliteCreateDatabaseUsingMetadata(mapId, urls)) {
                    Timber.e("Map Database wasn't created");
                    return;
                }

                Timber.i("Starting download process for map id " + mapId);
                startDownloading(mapId);
            }
        });
    }
}
