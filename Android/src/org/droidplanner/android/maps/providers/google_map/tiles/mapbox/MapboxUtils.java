package org.droidplanner.android.maps.providers.google_map.tiles.mapbox;

import android.content.Context;
import android.text.TextUtils;

import org.droidplanner.android.utils.NetworkUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 5/11/15.
 */
public class MapboxUtils {

    //Private constructor to prevent instantiation.
    private MapboxUtils(){}

    public static final int TILE_WIDTH = 512; //pixels
    public static final int TILE_HEIGHT = 512; //pixels

    public static final String MAPBOX_BASE_URL_V4 = "https://a.tiles.mapbox.com/v4/";

    public static String getMapTileURL(String userId, String mapID, String accessToken, int zoom, int x, int y) {
        return String.format(Locale.US, "https://api.mapbox.com/styles/v1/%s/%s/tiles/%d/%d/%d%s?access_token=%s",
                userId, mapID, zoom, x, y, "@2x", accessToken);
    }

    public static int fetchReferenceTileUrl(Context context, String userId, String mapId, String accessToken){
        if(!NetworkUtils.isNetworkAvailable(context)){
            Timber.d("Network is not available. Aborting reference tile fetching.");
            return -1;
        }

        final String referenceUrl = getMapTileURL(userId, mapId, accessToken, 0, 0, 0);

        HttpURLConnection conn = null;
        try{
            conn = NetworkUtils.getHttpURLConnection(new URL(referenceUrl));
            Timber.d("Download reference mapbox tile @ %s", referenceUrl);
            conn.setConnectTimeout(10000);
            conn.connect();
            int result = conn.getResponseCode();
            return result;
        }catch(IOException e){
            Timber.e(e, "Error while retrieving mapbox reference tile.");
        }
        finally{
            if(conn != null)
                conn.disconnect();
        }
            return -1;
    }

    public static String markerIconURL(String accessToken, String size, String symbol, String color) {
        // Make a string which follows the Mapbox Core API spec for stand-alone markers. This relies on the Mapbox API
        // for error checking.

        StringBuffer marker = new StringBuffer("pin-");
        final String lowerCaseSize = size.toLowerCase(Locale.US);

        if (lowerCaseSize.charAt(0) == 'l') {
            marker.append("l"); // large
        } else if (lowerCaseSize.charAt(0) == 's') {
            marker.append("s"); // small
        } else {
            marker.append("m"); // default to medium
        }

        if (!TextUtils.isEmpty(symbol)) {
            marker.append(String.format("-%s+", symbol));
        } else {
            marker.append("+");
        }

        marker.append(color.replaceAll("#", ""));

//        if (AppUtils.isRunningOn2xOrGreaterScreen(context)) {
//            marker.append("@2x");
//        }
        marker.append(".png");

        marker.append("?access_token=");
        marker.append(accessToken);
        return String.format(Locale.US, MAPBOX_BASE_URL_V4 + "marker/%s", marker);
    }

    public static String getUserAgent() {
        return "Mapbox Android SDK/0.7.3";
    }
}
