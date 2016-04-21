package org.droidplanner.android.maps.providers.google_map.tiles.arcgis

import com.google.android.gms.maps.model.UrlTileProvider
import org.droidplanner.android.maps.providers.google_map.tiles.arcgis.ArcGISTileProviderManager.MapType
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by fredia on 4/16/16.
 */
internal class ArcGisTileProvider(val mapType: MapType): UrlTileProvider(ArcGISTileProviderManager.TILE_WIDTH, ArcGISTileProviderManager.TILE_HEIGHT) {

    override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
        val tileUrl = mapType.getMapTypeUrl(zoom, x, y)
        if(tileUrl != null){
            try{
                return URL(tileUrl)
            } catch(e: MalformedURLException){
                Timber.e(e, "Error while building url for arc GIS map tile.")
            }
        }

        return null
    }
}