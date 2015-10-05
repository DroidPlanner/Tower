package org.droidplanner.android.maps.providers.google_map.tiles.mapbox;

import android.content.Context;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import org.droidplanner.android.data.DatabaseState;

/**
 * Created by Fredia Huya-Kouadio on 5/11/15.
 */
public class OfflineTileProvider implements TileProvider {

    private static final String TAG = OfflineTileProvider.class.getSimpleName();

    private final Context context;
    private final String mapboxId;
    private final String mapboxAccessToken;
    private final int maxZoomLevel;

    public OfflineTileProvider(Context context, String mapboxId, String mapboxAccessToken, int maxZoomLevel) {
        this.context = context;
        this.mapboxId = mapboxId;
        this.mapboxAccessToken = mapboxAccessToken;
        this.maxZoomLevel = maxZoomLevel;
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        if (zoom > maxZoomLevel) {
            return TileProvider.NO_TILE;
        }

        final String tileUri = MapboxUtils.getMapTileURL(mapboxId, mapboxAccessToken, zoom, x, y);
        byte[] data = DatabaseState.getOfflineDatabaseHandlerForMapId(context, mapboxId).dataForURL(tileUri);
        if (data == null || data.length == 0)
            return TileProvider.NO_TILE;

        return new Tile(MapboxUtils.TILE_WIDTH, MapboxUtils.TILE_HEIGHT, data);
    }
}
