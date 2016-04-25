package org.droidplanner.android.maps.providers.google_map.tiles.arcgis

import android.content.Context
import android.support.annotation.StringRes
import org.droidplanner.android.R
import org.droidplanner.android.maps.DPMap
import org.droidplanner.android.maps.providers.google_map.tiles.TileProviderManager
import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.offline.MapDownloader
import timber.log.Timber
import java.util.*

/**
 * Created by fredia on 4/16/16.
 *
 * Manager for the Arc GIS tile providers
 */
class ArcGISTileProviderManager(val context: Context, val selectedMap: String) :
        TileProviderManager(
                ArcGisTileProvider(selectMapType(context, selectedMap) ?: throw IllegalArgumentException("Selected map parameter is not supported.")),
                ArcGISOfflineTileProvider(context, selectMapType(context, selectedMap) ?: throw IllegalArgumentException("Selected map parameter is not supported."))){

    companion object {
        const val TILE_HEIGHT = 256
        const val TILE_WIDTH = 256

        internal fun selectMapType(context: Context, mapLabel: String): MapType? {
            var tempMap: MapType? = null
            for(map in MapType.values()){
                val label = context.getString(map.labelResId)
                if(mapLabel == label){
                    tempMap = map
                    break;
                }
            }

            return tempMap
        }
    }

    internal enum class MapType(@StringRes val labelResId: Int, val maxZoomLevel: Int, val baseUrl: String) {
        NAT_GEO_WORLD_MAP(R.string.label_nat_geo_world_map, 16, "http://services.arcgisonline.com/arcgis/rest/services/NatGeo_World_Map/MapServer/tile"),
        WORLD_IMAGERY(R.string.label_world_imagery, 23, "http://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile"),
        WORLD_STREET_MAP(R.string.label_world_street_map, 23, "http://services.arcgisonline.com/arcgis/rest/services/World_Street_Map/MapServer/tile"),
        WORLD_TOPO_MAP(R.string.label_world_topo_map, 23, "http://services.arcgisonline.com/arcgis/rest/services/World_Topo_Map/MapServer/tile");

        internal fun getMapTypeUrl(zoom: Int, x: Int, y: Int): String? {
            if(zoom <= maxZoomLevel){
                return "$baseUrl/$zoom/$y/$x"
            }

            return null
        }
    }

    private val mapType : MapType

    init {
        mapType = selectMapType(context, selectedMap) ?: throw IllegalArgumentException("Selected map parameter is not supported.")
    }

    override fun downloadMapTiles(mapDownloader: MapDownloader, mapRegion: DPMap.VisibleMapArea,
    minimumZ : Int, maximumZ : Int) {
        val urls = ArrayList<String>()

        // Loop through the zoom levels and lat/lon bounds to generate a list of urls which should be included in the offline map
        //
        val minLat = Math.min(
                Math.min(mapRegion.farLeft.latitude, mapRegion.nearLeft.latitude),
                Math.min(mapRegion.farRight.latitude, mapRegion.nearRight.latitude))
        val maxLat = Math.max(
                Math.max(mapRegion.farLeft.latitude, mapRegion.nearLeft.latitude),
                Math.max(mapRegion.farRight.latitude, mapRegion.nearRight.latitude))

        val minLon = Math.min(
                Math.min(mapRegion.farLeft.longitude, mapRegion.nearLeft.longitude),
                Math.min(mapRegion.farRight.longitude, mapRegion.nearRight.longitude))
        val maxLon = Math.max(
                Math.max(mapRegion.farLeft.longitude, mapRegion.nearLeft.longitude),
                Math.max(mapRegion.farRight.longitude, mapRegion.nearRight.longitude))

        var minX: Int
        var maxX: Int
        var minY: Int
        var maxY: Int
        var tilesPerSide: Int

        Timber.d("Generating urls for ArcGIS ${context.getString(mapType.labelResId)} tiles from zoom $minimumZ to zoom $maximumZ")

        for (zoom in minimumZ..maximumZ) {
            tilesPerSide = java.lang.Double.valueOf(Math.pow(2.0, zoom.toDouble()))!!.toInt()
            minX = java.lang.Double.valueOf(Math.floor((minLon + 180.0) / 360.0 * tilesPerSide))!!.toInt()
            maxX = java.lang.Double.valueOf(Math.floor((maxLon + 180.0) / 360.0 * tilesPerSide))!!.toInt()
            minY = java.lang.Double.valueOf(Math.floor((1.0 - Math.log(Math.tan(maxLat * Math.PI / 180.0) + 1.0 / Math.cos(maxLat * Math.PI / 180.0)) / Math.PI) / 2.0 * tilesPerSide))!!.toInt()
            maxY = java.lang.Double.valueOf(Math.floor((1.0 - Math.log(Math.tan(minLat * Math.PI / 180.0) + 1.0 / Math.cos(minLat * Math.PI / 180.0)) / Math.PI) / 2.0 * tilesPerSide))!!.toInt()
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    val url = mapType.getMapTypeUrl(zoom, x, y) ?: continue
                    urls.add(url)
                }
            }
        }

        Timber.d("${urls.size} urls generated for ArcGIS ${context.getString(mapType.labelResId)} tiles.")

        //Start downloading the tiles
        mapDownloader.startDownloadProcess(mapType.name, urls)
    }

}