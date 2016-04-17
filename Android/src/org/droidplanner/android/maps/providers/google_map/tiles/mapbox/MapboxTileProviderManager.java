package org.droidplanner.android.maps.providers.google_map.tiles.mapbox;

import android.content.Context;

import org.droidplanner.android.maps.providers.google_map.tiles.TileProviderManager;

/**
 * Created by fredia on 4/16/16.
 *
 * Manager for the mapbox online and offline tile providers
 */
public class MapboxTileProviderManager extends TileProviderManager {

    private final String mapboxId;
    private final String mapboxAccessToken;

    public MapboxTileProviderManager(Context context, String mapboxId, String mapboxAccessToken, int maxZoomLevel) {
        super(new MapboxTileProvider(mapboxId, mapboxAccessToken, maxZoomLevel),
            new OfflineTileProvider(context, mapboxId, mapboxAccessToken, maxZoomLevel));

        this.mapboxId = mapboxId;
        this.mapboxAccessToken = mapboxAccessToken;
    }

    public String getMapboxAccessToken() {
        return mapboxAccessToken;
    }

    public String getMapboxId() {
        return mapboxId;
    }
}
