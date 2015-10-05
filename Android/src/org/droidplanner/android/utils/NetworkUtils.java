package org.droidplanner.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.MapboxUtils;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Fredia Huya-Kouadio on 5/11/15.
 */
public class NetworkUtils {

    private static final String SOLO_LINK_WIFI_PREFIX = "SoloLink_";

    public static boolean isNetworkAvailable(Context context) {
        if(context == null)
            return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isOnSoloNetwork(Context context) {
        if (context == null){
            return false;
        }

        final String connectedSSID = getCurrentWifiLink(context);
        return connectedSSID != null && connectedSSID.startsWith(SOLO_LINK_WIFI_PREFIX);
    }

    public static String getCurrentWifiLink(Context context) {
        if(context == null)
            return null;

        final WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        final WifiInfo connectedWifi = wifiMgr.getConnectionInfo();
        final String connectedSSID = connectedWifi == null ? null : connectedWifi.getSSID().replace("\"", "");
        return connectedSSID;
    }

    public static HttpURLConnection getHttpURLConnection(final URL url) {
        return getHttpURLConnection(url, null, null);
    }

    public static HttpURLConnection getHttpURLConnection(final URL url, final Cache cache) {
        return getHttpURLConnection(url, cache, null);
    }

    public static HttpURLConnection getHttpURLConnection(final URL url, final Cache cache, final SSLSocketFactory sslSocketFactory) {
        OkHttpClient client = new OkHttpClient();
        if (cache != null) {
            client.setCache(cache);
        }
        if (sslSocketFactory != null) {
            client.setSslSocketFactory(sslSocketFactory);
        }
        HttpURLConnection connection = new OkUrlFactory(client).open(url);
        connection.setRequestProperty("User-Agent", MapboxUtils.getUserAgent());
        return connection;
    }
}
