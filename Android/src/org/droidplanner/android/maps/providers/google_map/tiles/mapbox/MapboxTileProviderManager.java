package org.droidplanner.android.maps.providers.google_map.tiles.mapbox;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;

import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.providers.google_map.tiles.TileProviderManager;
import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.offline.MapDownloader;
import org.droidplanner.android.utils.NetworkUtils;
import org.droidplanner.android.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by fredia on 4/16/16.
 *
 * Manager for the mapbox online and offline tile providers
 */
public class MapboxTileProviderManager extends TileProviderManager {

    private final Handler handler = new Handler();

    private final Context context;
    private final String mapboxId;
    private final String mapboxAccessToken;

    public MapboxTileProviderManager(Context context, String mapboxId, String mapboxAccessToken, int maxZoomLevel) {
        super(new MapboxTileProvider(mapboxId, mapboxAccessToken, maxZoomLevel),
            new OfflineTileProvider(context, mapboxId, mapboxAccessToken, maxZoomLevel));

        this.context = context;
        this.mapboxId = mapboxId;
        this.mapboxAccessToken = mapboxAccessToken;
    }

    public String getMapboxAccessToken() {
        return mapboxAccessToken;
    }

    public String getMapboxId() {
        return mapboxId;
    }

    @Override
    public void downloadMapTiles(MapDownloader mapDownloader, DPMap.VisibleMapArea mapRegion, int
        minimumZ, int maximumZ) {
        beginDownloadingMapID(mapDownloader, this.mapboxId, this.mapboxAccessToken, mapRegion, minimumZ, maximumZ);
    }

    private void beginDownloadingMapID(final MapDownloader mapDownloader, final String mapId, final String accessToken, DPMap.VisibleMapArea mapRegion, int
        minimumZ, int maximumZ) {
        beginDownloadingMapID(mapDownloader, mapId, accessToken, mapRegion, minimumZ, maximumZ, true, true);
    }

    private void beginDownloadingMapID(final MapDownloader mapDownloader, final String mapId, final String accessToken, DPMap.VisibleMapArea mapRegion, int
        minimumZ, int maximumZ, boolean includeMetadata,
                                      boolean includeMarkers) {

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

        Timber.d("Generating urls for mapbox tiles from zoom " + minimumZ + " to zoom " + maximumZ);

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

        Timber.d(urls.size() + " urls generated for mapbox tiles.");

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
                mapDownloader.notifyDelegateOfNetworkConnectivityError(new IllegalStateException("Network is unavailable"));
                Timber.e("Network is unavailable.");
                return;
            }

            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
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
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mapDownloader.startDownloadProcess(mapId, urls);
                            }
                        });
                    }
                }
            });
        } else {
            Timber.i("No marker icons to worry about, so just start downloading.");
            // There aren't any marker icons to worry about, so just create database and start downloading
            mapDownloader.startDownloadProcess(mapId, urls);
        }

    }

    private static Set<String> parseMarkerIconURLStringsFromGeojsonData(String accessToken, String data) {
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
