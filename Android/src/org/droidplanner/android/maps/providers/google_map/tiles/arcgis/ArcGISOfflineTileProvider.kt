package org.droidplanner.android.maps.providers.google_map.tiles.arcgis

import android.content.Context
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import org.droidplanner.android.data.DatabaseState

/**
 * Created by fredia on 4/20/16.
 */
internal class ArcGISOfflineTileProvider(val context: Context, val mapType: ArcGISTileProviderManager.MapType) : TileProvider {
    override fun getTile(x: Int, y: Int, zoom: Int): Tile {
        if(zoom > mapType.maxZoomLevel)
            return TileProvider.NO_TILE

        val tileUri = mapType.getMapTypeUrl(zoom, x, y) ?: return TileProvider.NO_TILE
        val data = DatabaseState.getOfflineDatabaseHandlerForMapId(context, mapType.name).dataForURL(tileUri)
        if(data == null || data.size == 0)
            return TileProvider.NO_TILE

        return Tile(ArcGISTileProviderManager.TILE_WIDTH, ArcGISTileProviderManager.TILE_HEIGHT, data)
    }
}