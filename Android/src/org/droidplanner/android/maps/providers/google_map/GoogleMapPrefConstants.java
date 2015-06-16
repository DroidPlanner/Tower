package org.droidplanner.android.maps.providers.google_map;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Fredia Huya-Kouadio on 6/16/15.
 */
public class GoogleMapPrefConstants {

    @StringDef({MAPBOX_AUTO_TILE_SOURCE, MAPBOX_ONLINE_TILE_SOURCE, MAPBOX_OFFLINE_TILE_SOURCE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MapboxTileSource {}

    public static final String MAPBOX_AUTO_TILE_SOURCE = "auto";
    public static final String MAPBOX_ONLINE_TILE_SOURCE = "online";
    public static final String MAPBOX_OFFLINE_TILE_SOURCE = "offline";


    @StringDef({GOOGLE_TILE_PROVIDER, MAPBOX_TILE_PROVIDER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TileProvider{}

    public static final String GOOGLE_TILE_PROVIDER = "google";
    public static final String MAPBOX_TILE_PROVIDER = "mapbox";

    //Prevent instantiation
    private GoogleMapPrefConstants(){}
}
