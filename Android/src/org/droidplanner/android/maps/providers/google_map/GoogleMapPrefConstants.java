package org.droidplanner.android.maps.providers.google_map;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Fredia Huya-Kouadio on 6/16/15.
 */
public class GoogleMapPrefConstants {

    @StringDef({GOOGLE_TILE_PROVIDER, MAPBOX_TILE_PROVIDER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TileProvider{}

    public static final String GOOGLE_TILE_PROVIDER = "google";
    public static final String MAPBOX_TILE_PROVIDER = "mapbox";


    static final String DEFAULT_TILE_PROVIDER = GOOGLE_TILE_PROVIDER;

    static final String MAP_TYPE_SATELLITE = "satellite";
    static final String MAP_TYPE_HYBRID = "hybrid";
    static final String MAP_TYPE_NORMAL = "normal";
    static final String MAP_TYPE_TERRAIN = "terrain";

    static final String PREF_TILE_PROVIDERS = "pref_google_map_tile_providers";

    static final String PREF_GOOGLE_TILE_PROVIDER_SETTINGS = "pref_google_tile_provider_settings";

    static final String PREF_MAP_TYPE = "pref_map_type";
    static final String DEFAULT_MAP_TYPE = MAP_TYPE_SATELLITE;

    static final String PREF_MAPBOX_TILE_PROVIDER_SETTINGS = "pref_mapbox_tile_provider_settings";

    static final String PREF_MAPBOX_MAP_DOWNLOAD = "pref_mapbox_map_download";

    static final String PREF_DOWNLOAD_MENU_OPTION = "pref_download_menu_option";
    static final boolean DEFAULT_DOWNLOAD_MENU_OPTION = false;

    static final String PREF_MAPBOX_ID = "pref_mapbox_id";
    static final String PREF_MAPBOX_ACCESS_TOKEN = "pref_mapbox_access_token";

    static final String PREF_MAPBOX_LEARN_MORE = "pref_mapbox_learn_more";

    static final String PREF_ENABLE_OFFLINE_LAYER = "pref_enable_offline_map_layer";
    static final boolean DEFAULT_OFFLINE_LAYER_ENABLED = false;

    //Prevent instantiation
    private GoogleMapPrefConstants(){}
}
