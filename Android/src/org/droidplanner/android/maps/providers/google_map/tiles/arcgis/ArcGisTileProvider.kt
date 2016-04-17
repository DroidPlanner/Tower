package org.droidplanner.android.maps.providers.google_map.tiles.arcgis

import android.content.Context
import android.support.annotation.StringRes
import com.google.android.gms.maps.model.UrlTileProvider
import org.droidplanner.android.R
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by fredia on 4/16/16.
 */
class ArcGisTileProvider(context: Context, val selectedMap: String): UrlTileProvider(TILE_WIDTH, TILE_HEIGHT) {

    private val mapType: MapType

    init {
        val prefManager = DroidPlannerPrefs.getInstance(context)

        //Retrieve the map type set in the preferences
        var tempMap: MapType? = null
        for(map in MapType.values()){
            val label = context.getString(map.labelResId)
            if(selectedMap == label){
                tempMap = map
                break;
            }
        }

        if(tempMap == null)
            throw IllegalArgumentException("Selected map parameter is not supported.")

        mapType = tempMap
    }

    companion object {
        const val TILE_HEIGHT = 256
        const val TILE_WIDTH = 256
    }

    private enum class MapType(@StringRes val labelResId: Int, val maxZoomLevel: Int, val baseUrl: String) {
        NAT_GEO_WORLD_MAP(R.string.label_nat_geo_world_map, 16, "http://services.arcgisonline.com/arcgis/rest/services/NatGeo_World_Map/MapServer/tile"),
        WORLD_IMAGERY(R.string.label_world_imagery, 23, "http://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile"),
        WORLD_PHYSICAL_MAP(R.string.label_world_physical_map, 8, "http://services.arcgisonline.com/arcgis/rest/services/World_Physical_Map/MapServer/tile"),
        WORLD_SHADED_RELIEF(R.string.label_world_shaded_relief, 13, "http://services.arcgisonline.com/arcgis/rest/services/World_Shaded_Relief/MapServer/tile"),
        WORLD_STREET_MAP(R.string.label_world_street_map, 23, "http://services.arcgisonline.com/arcgis/rest/services/World_Street_Map/MapServer/tile"),
        WORLD_TERRAIN_BASE(R.string.label_world_terrain_base, 13, "http://services.arcgisonline.com/arcgis/rest/services/World_Terrain_Base/MapServer/tile"),
        WORLD_TOPO_MAP(R.string.label_world_topo_map, 23, "http://services.arcgisonline.com/arcgis/rest/services/World_Topo_Map/MapServer/tile")
    }

    override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
        if(zoom <= mapType.maxZoomLevel){
            val tileUrl = "${mapType.baseUrl}/$zoom/$y/$x"
            try{
                return URL(tileUrl)
            } catch(e: MalformedURLException){
                Timber.e(e, "Error while building url for arc GIS map tile.")
            }
        }

        return null
    }
}