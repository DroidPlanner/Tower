package org.droidplanner.android.maps.providers.google_map.tiles.mapbox.offline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.VisibleRegion;

import org.droidplanner.android.data.DatabaseState;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.MapboxUtils;
import org.droidplanner.android.utils.NetworkUtils;
import org.droidplanner.android.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

public class MapDownloader {

    /**
     * The possible states of the offline map downloader.
     */
    public enum MBXOfflineMapDownloaderState {
        /**
         * An offline map download job is in progress.
         */
        MBXOfflineMapDownloaderStateRunning,
        /**
         * An offline map download job is being canceled.
         */
        MBXOfflineMapDownloaderStateCanceling,
        /**
         * The offline map downloader is ready to begin a new offline map download job.
         */
        MBXOfflineMapDownloaderStateAvailable
    }

    private MBXOfflineMapDownloaderState state;
    private final AtomicInteger totalFilesWritten = new AtomicInteger(0);
    private final AtomicInteger totalFilesExpectedToWrite = new AtomicInteger(0);

    private final Context context;
    private ExecutorService downloadsScheduler;
    private final ArrayList<MapDownloaderListener> listeners = new ArrayList<>();

    public MapDownloader(Context context) {
        this.context = context;
        setupDownloadScheduler();

        this.state = MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateAvailable;
    }

    public MBXOfflineMapDownloaderState getState() {
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
        if (state == MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateRunning) {
            this.state = MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateCanceling;
            notifyDelegateOfStateChange();
        }

        setupDownloadScheduler();

        if (state == MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateCanceling) {
            this.state = MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateAvailable;
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

    public void startDownloading(final String mapId) {

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
        if (this.state != MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateRunning) {
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
        if (this.state == MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateRunning) {
            Timber.i("Just finished downloading all materials.  Persist the OfflineMapDatabase, change the state, and call it a day.");
            // This is what to do when we've downloaded all the files
            notifyDelegateOfCompletionWithOfflineMapDatabase();
            this.state = MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateAvailable;
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

    public void beginDownloadingMapID(final String mapId, final String accessToken, DPMap.VisibleMapArea mapRegion, int
            minimumZ, int maximumZ) {
        beginDownloadingMapID(mapId, accessToken, mapRegion, minimumZ, maximumZ, true, true);
    }

    public void beginDownloadingMapID(final String mapId, final String accessToken, DPMap.VisibleMapArea mapRegion, int
            minimumZ, int maximumZ, boolean includeMetadata,
                                      boolean includeMarkers) {
        if (state != MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateAvailable) {
            Timber.w("state doesn't equal MBXOfflineMapDownloaderStateAvailable so return.  state = " + state);
            return;
        }

        // Start a download job to retrieve all the resources needed for using the specified map offline
        this.state = MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateRunning;
        notifyDelegateOfStateChange();

        final ArrayList<String> urls = new ArrayList<String>();
        String dataName = "features.json";    // Only using API V4 for now

        // Include URLs for the metadata and markers json if applicable
        if (includeMetadata) {
            urls.add(String.format(Locale.US, MapboxUtils.MAPBOX_BASE_URL_V4 + "%s.json?secure&access_token=%s",
                    mapId, accessToken));
        }
        if (includeMarkers) {
            urls.add(String.format(Locale.US, MapboxUtils.MAPBOX_BASE_URL_V4 + "%s/%s?access_token=%s", mapId,
                    dataName, accessToken));
        }

        // Loop through the zoom levels and lat/lon bounds to generate a list of urls which should be included in the offline map
        //
        double minLat = Math.min(
                Math.min(mapRegion.farLeft.getLatitude(), mapRegion.nearLeft.getLatitude()),
                Math.min(mapRegion.farRight.getLatitude(), mapRegion.nearRight.getLatitude()));
        double maxLat = Math.max(
                Math.max(mapRegion.farLeft.getLatitude(), mapRegion.nearLeft.getLatitude()),
                Math.max(mapRegion.farRight.getLatitude(), mapRegion.nearRight.getLatitude()));

        double minLon = Math.min(
                Math.min(mapRegion.farLeft.getLongitude(), mapRegion.nearLeft.getLongitude()),
                Math.min(mapRegion.farRight.getLongitude(), mapRegion.nearRight.getLongitude()));
        double maxLon = Math.max(
                Math.max(mapRegion.farLeft.getLongitude(), mapRegion.nearLeft.getLongitude()),
                Math.max(mapRegion.farRight.getLongitude(), mapRegion.nearRight.getLongitude()));

        int minX;
        int maxX;
        int minY;
        int maxY;
        int tilesPerSide;

        Timber.d("Generating urls for tiles from zoom " + minimumZ + " to zoom " + maximumZ);

        for (int zoom = minimumZ; zoom <= maximumZ; zoom++) {
            tilesPerSide = Double.valueOf(Math.pow(2.0, zoom)).intValue();
            minX = Double.valueOf(Math.floor(((minLon + 180.0) / 360.0) * tilesPerSide)).intValue();
            maxX = Double.valueOf(Math.floor(((maxLon + 180.0) / 360.0) * tilesPerSide)).intValue();
            minY = Double.valueOf(Math.floor((1.0 - (Math.log(Math.tan(maxLat * Math.PI / 180.0) + 1.0 / Math.cos(maxLat * Math.PI / 180.0)) / Math.PI)) / 2.0 * tilesPerSide)).intValue();
            maxY = Double.valueOf(Math.floor((1.0 - (Math.log(Math.tan(minLat * Math.PI / 180.0) + 1.0 / Math.cos(minLat * Math.PI / 180.0)) / Math.PI)) / 2.0 * tilesPerSide)).intValue();
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    urls.add(MapboxUtils.getMapTileURL(mapId, accessToken, zoom, x, y));
                }
            }
        }

        Timber.d(urls.size() + " urls generated.");

        // Determine if we need to add marker icon urls (i.e. parse markers.geojson/features.json), and if so, add them
        if (includeMarkers) {
            String dName = "markers.geojson";
            final String geojson = String.format(Locale.US, MapboxUtils.MAPBOX_BASE_URL_V4 + "%s/%s?access_token=%s",
                    mapId, dName, accessToken);

            if (!NetworkUtils.isNetworkAvailable(context)) {
                // We got a session level error which probably indicates a connectivity problem such as airplane mode.
                // Since we must fetch and parse markers.geojson/features.json in order to determine which marker icons need to be
                // added to the list of urls to download, the lack of network connectivity is a non-recoverable error
                // here.
                notifyDelegateOfNetworkConnectivityError(new IllegalStateException("Network is unavailable"));
                Timber.e("Network is unavailable.");
                return;
            }

            downloadsScheduler.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpURLConnection conn = NetworkUtils.getHttpURLConnection(new URL(geojson));
                        conn.setConnectTimeout(60000);
                        conn.connect();
                        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            throw new IOException();
                        }

                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
                        String jsonText = Utils.readAll(rd);

                        // The marker geojson was successfully retrieved, so parse it for marker icons. Note that we shouldn't
                        // try to save it here, because it may already be in the download queue and saving it twice will mess
                        // up the count of urls to be downloaded!
                        //
                        Set<String> markerIconURLStrings = new HashSet<String>();
                        markerIconURLStrings.addAll(parseMarkerIconURLStringsFromGeojsonData(accessToken, jsonText));
                        Timber.i("Number of markerIconURLs = " + markerIconURLStrings.size());
                        if (markerIconURLStrings.size() > 0) {
                            urls.addAll(markerIconURLStrings);
                        }
                    } catch (IOException e) {
                        // The url for markers.geojson/features.json didn't work (some maps don't have any markers). Notify the delegate of the
                        // problem, and stop attempting to add marker icons, but don't bail out on whole the offline map download.
                        // The delegate can decide for itself whether it wants to continue or cancel.
                        //
                        // TODO
                        e.printStackTrace();
/*
                        [self notifyDelegateOfHTTPStatusError:((NSHTTPURLResponse *)response).statusCode url:response.URL];
*/
                    } finally {
                        startDownloadProcess(mapId, urls);
                    }
                }
            });
        } else {
            Timber.i("No marker icons to worry about, so just start downloading.");
            // There aren't any marker icons to worry about, so just create database and start downloading
            startDownloadProcess(mapId, urls);
        }

    }

    /**
     * Private method for Starting the Whole Download Process
     *
     * @param urls Map urls
     */
    private void startDownloadProcess(final String mapId, final List<String> urls) {
        downloadsScheduler.execute(new Runnable() {
            @Override
            public void run() {
                deleteIncompleteDownloads(mapId);

                // Do database creation / io on background thread
                if (!sqliteCreateDatabaseUsingMetadata(mapId, urls)) {
                    Timber.e("Map Database wasn't created");
                    return;
                }

                startDownloading(mapId);
            }
        });
    }


    public Set<String> parseMarkerIconURLStringsFromGeojsonData(String accessToken, String data) {
        HashSet<String> iconURLStrings = new HashSet<String>();

        JSONObject simplestyleJSONDictionary;
        try {
            simplestyleJSONDictionary = new JSONObject(data);

            // Find point features in the markers dictionary (if there are any) and add them to the map.
            JSONArray markers = simplestyleJSONDictionary.optJSONArray("features");

            if (markers != null && markers.length() > 0) {
                for (int lc = 0; lc < markers.length(); lc++) {
                    JSONObject feature = markers.optJSONObject(lc);
                    if (feature != null) {
                        String type = feature.getJSONObject("geometry").getString("type");

                        if ("Point".equals(type)) {
                            String size = feature.getJSONObject("properties").getString("marker-size");
                            String color = feature.getJSONObject("properties").getString("marker-color");
                            String symbol = feature.getJSONObject("properties").getString("marker-symbol");
                            if (!TextUtils.isEmpty(size) && !TextUtils.isEmpty(color) && !TextUtils.isEmpty(symbol)) {
                                String markerURL = MapboxUtils.markerIconURL(accessToken, size, symbol, color);
                                if (!TextUtils.isEmpty(markerURL)) {
                                    iconURLStrings.add(markerURL);
                                }
                            }
                        }
                    }
                    // This is the last line of the loop
                }
            }
        } catch (JSONException e) {
            Timber.e(e, e.getMessage());
        }

        // Return only the unique icon urls
        return iconURLStrings;
    }

}
