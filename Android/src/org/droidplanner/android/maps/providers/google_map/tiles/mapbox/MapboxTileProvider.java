package org.droidplanner.android.maps.providers.google_map.tiles.mapbox;

import android.util.Log;

import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Fredia Huya-Kouadio on 5/11/15.
 */
public class MapboxTileProvider extends UrlTileProvider {

    private final static String TAG = MapboxTileProvider.class.getSimpleName();

    private final String mapboxId;
    private final String mapboxAccessToken;
    private final int maxZoomLevel;

    public MapboxTileProvider(String mapboxId, String mapboxAccessToken, int maxZoomLevel) {
        super(MapboxUtils.TILE_WIDTH, MapboxUtils.TILE_HEIGHT);
        this.mapboxId = mapboxId;
        this.mapboxAccessToken = mapboxAccessToken;
        this.maxZoomLevel = maxZoomLevel;
    }

    @Override
    public URL getTileUrl(int x, int y, int zoom) {
        if (zoom <= maxZoomLevel) {
            final String tileUrl = MapboxUtils.getMapTileURL(mapboxId, mapboxAccessToken, zoom, x, y);
            try {
                return new URL(tileUrl);
            } catch (MalformedURLException e) {
                Log.e(TAG, "Error while building url for mapbox map tile.", e);
            }
        }
        return null;
    }

    public String getMapboxAccessToken() {
        return mapboxAccessToken;
    }

    public String getMapboxId() {
        return mapboxId;
    }
}
