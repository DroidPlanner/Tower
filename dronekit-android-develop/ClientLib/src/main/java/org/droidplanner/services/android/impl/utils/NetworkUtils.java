package org.droidplanner.services.android.impl.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import com.o3dr.android.client.BuildConfig;

import org.droidplanner.services.android.impl.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.SoloComp;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.Socket;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 5/11/15.
 */
public class NetworkUtils {
    /**
     * Is internet connection available. This method also returns true for the SITL build type
     * @param context
     * @return Internet connection availability.
     */
    public static boolean isNetworkAvailable(Context context) {
        if (!BuildConfig.SITL_DEBUG && isOnSololinkNetwork(context))
            return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getCurrentWifiLink(Context context) {
        final WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        final WifiInfo connectedWifi = wifiMgr.getConnectionInfo();
        final String connectedSSID = connectedWifi == null ? null : connectedWifi.getSSID().replace("\"", "");
        return connectedSSID;
    }

    public static boolean isOnSololinkNetwork(Context context) {
        if (BuildConfig.SITL_DEBUG)
            return true;

        final String connectedSSID = getCurrentWifiLink(context);
        return isSoloNetwork(connectedSSID);
    }

    public static boolean isSoloNetwork(String ssid) {
        return ssid != null && ssid.startsWith(SoloComp.SOLO_LINK_WIFI_PREFIX);
    }

    public static void bindSocketToNetwork(Bundle extras, Socket socket) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network network = extras == null
                ? null
                : (Network) extras.getParcelable(MavLinkConnection.EXTRA_NETWORK);
            bindSocketToNetwork(network, socket);
        }
    }

    @TargetApi(21)
    public static void bindSocketToNetwork(Network network, Socket socket) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (network != null && socket != null) {
                Timber.d("Binding socket to network %s", network);
                network.bindSocket(socket);
            }
        }
    }

    public static void bindSocketToNetwork(Bundle extras, DatagramSocket socket) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network network = extras == null
                ? null
                : (Network) extras.getParcelable(MavLinkConnection.EXTRA_NETWORK);
            bindSocketToNetwork(network, socket);
        }
    }

    @TargetApi(21)
    public static void bindSocketToNetwork(Network network, DatagramSocket socket) throws IOException {
        if (network != null && socket != null) {
            Timber.d("Binding datagram socket to network %s", network);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                network.bindSocket(socket);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Can be accessed through reflection.
                try {
                    Method bindSocketMethod = Network.class.getMethod("bindSocket", DatagramSocket.class);
                    bindSocketMethod.invoke(network, socket);
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    Timber.e(e, "Unable to access Network#bindSocket(DatagramSocket).");
                } catch (InvocationTargetException e) {
                    Timber.e(e, "Unable to invoke Network#bindSocket(DatagramSocket).");
                }
            }
        }
    }
}
