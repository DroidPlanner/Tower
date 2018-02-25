package org.droidplanner.services.android.impl.communication.connection;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;

import org.droidplanner.services.android.impl.utils.connection.WifiConnectionHandler;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import timber.log.Timber;

/**
 * Abstract the connection to a Solo vehicle.
 * Created by Fredia Huya-Kouadio on 12/17/15.
 */
public class SoloConnection extends AndroidMavLinkConnection implements WifiConnectionHandler.WifiConnectionListener {

    private static final int SOLO_UDP_PORT = 14550;

    private final WifiConnectionHandler wifiHandler;
    private final AndroidUdpConnection dataLink;
    private final String soloLinkId;
    private final String soloLinkPassword;

    public SoloConnection(Context applicationContext, String soloLinkId, String password) {
        super(applicationContext);
        this.wifiHandler = new WifiConnectionHandler(applicationContext);
        wifiHandler.setListener(this);

        this.soloLinkId = soloLinkId;
        this.soloLinkPassword = password;
        this.dataLink = new AndroidUdpConnection(applicationContext, SOLO_UDP_PORT) {
            @Override
            protected void onConnectionOpened(Bundle extras) {
                SoloConnection.this.onConnectionOpened(extras);
            }

            @Override
            protected void onConnectionStatus(LinkConnectionStatus connectionStatus) {
                SoloConnection.this.onConnectionStatus(connectionStatus);
            }
        };
    }

    @Override
    protected void openConnection(Bundle connectionExtras) throws IOException {
        if (TextUtils.isEmpty(soloLinkId)) {
            LinkConnectionStatus connectionStatus = LinkConnectionStatus
                .newFailedConnectionStatus(LinkConnectionStatus.INVALID_CREDENTIALS, "Invalid connection credentials!");
            onConnectionStatus(connectionStatus);
        } else {
            wifiHandler.start();
            checkScanResults(wifiHandler.getScanResults());
        }
    }

    private void refreshWifiAps() {
        if (!wifiHandler.refreshWifiAPs()) {
            LinkConnectionStatus connectionStatus = LinkConnectionStatus
                .newFailedConnectionStatus(LinkConnectionStatus.SYSTEM_UNAVAILABLE, "Unable to refresh wifi access points");
            onConnectionStatus(connectionStatus);
        }
    }

    @Override
    protected int readDataBlock(byte[] buffer) throws IOException {
        return dataLink.readDataBlock(buffer);
    }

    @Override
    protected void sendBuffer(byte[] buffer) throws IOException {
        dataLink.sendBuffer(buffer);
    }

    @Override
    protected void closeConnection() throws IOException {
        wifiHandler.stop();
        dataLink.closeConnection();
    }

    @Override
    protected void loadPreferences() {
        dataLink.loadPreferences();
    }

    @Override
    public int getConnectionType() {
        return dataLink.getConnectionType();
    }

    @Override
    public void onWifiConnected(String wifiSsid, Bundle extras) {
        if (isConnecting()) {
            //Let's see if we're connected to our target wifi
            if (wifiSsid.equalsIgnoreCase(soloLinkId)) {
                //We're good to go
                try {
                    dataLink.openConnection(extras);
                } catch (IOException e) {
                    reportIOException(e);
                    Timber.e(e, e.getMessage());
                }
            }
        }
    }

    @Override
    public void onWifiConnecting() {
        onConnectionStatus(new LinkConnectionStatus(LinkConnectionStatus.CONNECTING, null));
    }

    @Override
    public void onWifiDisconnected(String prevSsid) {
        if (prevSsid.equalsIgnoreCase(soloLinkId)) {
            onConnectionStatus(new LinkConnectionStatus(LinkConnectionStatus.DISCONNECTED, null));
        }
    }

    @Override
    public void onWifiScanResultsAvailable(List<ScanResult> results) {
        checkScanResults(results);
    }

    @Override
    public void onWifiConnectionFailed(LinkConnectionStatus connectionStatus) {
        onConnectionStatus(connectionStatus);
    }

    private void checkScanResults(List<ScanResult> results) {
        if (!isConnecting())
            return;

        //We're in the connection process, let's see if the wifi we want is available
        ScanResult targetResult = null;
        for (ScanResult result : results) {
            if (result.SSID.equalsIgnoreCase(this.soloLinkId)) {
                //bingo
                targetResult = result;
                break;
            }
        }

        if (targetResult != null) {
            //We're good to go
            try {
                Bundle connectInfo = new Bundle();
                Bundle extras = getConnectionExtras();
                if (extras != null && !extras.isEmpty()) {
                    connectInfo.putAll(extras);
                }
                connectInfo.putParcelable(WifiConnectionHandler.EXTRA_SCAN_RESULT, targetResult);
                connectInfo.putString(WifiConnectionHandler.EXTRA_SSID_PASSWORD, soloLinkPassword);
                int connectionResult = wifiHandler.connectToWifi(connectInfo);
                if (connectionResult != 0) {
                    @LinkConnectionStatus.FailureCode int failureCode = connectionResult;
                    LinkConnectionStatus connectionStatus = LinkConnectionStatus
                        .newFailedConnectionStatus(failureCode, "Unable to connect to the target wifi " + soloLinkId);
                    onConnectionStatus(connectionStatus);
                }
            } catch (IllegalArgumentException e) {
                Timber.e(e, e.getMessage());
                LinkConnectionStatus connectionStatus = LinkConnectionStatus.newFailedConnectionStatus(LinkConnectionStatus.UNKNOWN, e.getMessage());
                onConnectionStatus(connectionStatus);
            }
        } else {
            //Let's try again
            refreshWifiAps();
        }
    }

    private boolean isConnecting() {
        return getConnectionStatus() == MAVLINK_CONNECTING;
    }

    public static boolean isUdpSoloConnection(Context context, ConnectionParameter connParam){
        if(connParam == null)
            return false;

        final int connectionType = connParam.getConnectionType();
        switch(connectionType){
            case ConnectionType.TYPE_UDP:
                Bundle paramsBundle = connParam.getParamsBundle();
                if(paramsBundle == null)
                    return false;

                final int serverPort = paramsBundle.getInt(ConnectionType.EXTRA_UDP_SERVER_PORT, ConnectionType.DEFAULT_UDP_SERVER_PORT);
                final String wifiSsid = WifiConnectionHandler.getCurrentWifiLink((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
                return WifiConnectionHandler.isSoloWifi(wifiSsid) && serverPort == SOLO_UDP_PORT;

            default:
                return false;
        }
    }

    public static ConnectionParameter getSoloConnectionParameterFromUdp(Context context, ConnectionParameter udpConnectionParameters){
        if(context == null)
            return null;

        final String wifiSsid = WifiConnectionHandler.getCurrentWifiLink((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
        if(WifiConnectionHandler.isSoloWifi(wifiSsid)){
            return ConnectionParameter.newSoloConnection(wifiSsid, null, udpConnectionParameters.getTLogLoggingUri(), udpConnectionParameters.getEventsDispatchingPeriod());
        }

        return null;
    }
}
