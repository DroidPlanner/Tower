package org.droidplanner.android.maps.providers.google_map.tiles.arcgis

import android.content.Context
import org.droidplanner.android.maps.providers.google_map.tiles.TileProviderManager

/**
 * Created by fredia on 4/16/16.
 *
 * Manager for the Arc GIS tile providers
 */
class ArcGISTileProviderManager(context: Context, val selectedMap: String) : TileProviderManager(ArcGisTileProvider(context, selectedMap), null)