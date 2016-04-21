package org.droidplanner.android.maps.providers.google_map.tiles;

import com.google.android.gms.maps.model.TileProvider;

import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.offline.MapDownloader;

/**
 * Created by fredia on 4/16/16.
 */
public abstract class TileProviderManager {

    protected final TileProvider onlineTileProvider;
    protected final TileProvider offlineTileProvider;

    protected TileProviderManager(TileProvider onlineTileProvider, TileProvider offlineTileProvider) {
        this.offlineTileProvider = offlineTileProvider;
        this.onlineTileProvider = onlineTileProvider;
    }

    public TileProvider getOfflineTileProvider() {
        return offlineTileProvider;
    }

    public TileProvider getOnlineTileProvider() {
        return onlineTileProvider;
    }

    public abstract void downloadMapTiles(MapDownloader mapDownloader, DPMap.VisibleMapArea mapRegion, int
        minimumZ, int maximumZ);
}
