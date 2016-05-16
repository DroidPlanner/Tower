package org.droidplanner.android.maps.providers.google_map;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Fredia Huya-Kouadio on 6/16/15.
 */
public class GoogleMapPrefConstants {

    @StringDef({GOOGLE_TILE_PROVIDER, MAPBOX_TILE_PROVIDER, ARC_GIS_TILE_PROVIDER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TileProvider{}

    public static final String GOOGLE_TILE_PROVIDER = "google";
    public static final String MAPBOX_TILE_PROVIDER = "mapbox";
    public static final String ARC_GIS_TILE_PROVIDER = "arcgis";

    //Prevent instantiation
    private GoogleMapPrefConstants(){}
}
