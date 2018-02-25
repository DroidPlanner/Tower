package org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.sololink;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.o3dr.android.client.BuildConfig;
import com.o3dr.android.client.utils.connection.TcpConnection;
import com.o3dr.android.client.utils.connection.UdpConnection;
import com.o3dr.services.android.lib.drone.companion.solo.button.ButtonTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSetting;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSettingGetter;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSettingSetter;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproRequestState;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloMessageShotManagerError;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageParser;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import org.droidplanner.services.android.impl.communication.model.DataLink;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.AbstractLinkManager;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.SoloComp;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.controller.ControllerLinkManager;
import org.droidplanner.services.android.impl.utils.connection.SshConnection;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

/**
 * Handles solo link related logic.
 */
public class SoloLinkManager extends AbstractLinkManager<SoloLinkListener> {

    public static final String SOLO_LINK_IP = "10.1.1.10";
    public static final int SOLO_LINK_TCP_PORT = 5507;

    private static final int SHOT_FOLLOW_UDP_PORT = 14558;

    private static final String SOLO_VERSION_FILENAME = "/VERSION";
    private static final String PIXHAWK_VERSION_FILENAME = "/PIX_VERSION";
    private static final String GIMBAL_VERSION_FILENAME = "/AXON_VERSION";

    private final UdpConnection followDataConn;

    private final SshConnection sshLink;

    private final SoloButtonSettingGetter presetButtonAGetter = new SoloButtonSettingGetter(ButtonTypes.BUTTON_A,
        ButtonTypes.BUTTON_EVENT_PRESS);

    private final SoloButtonSettingGetter presetButtonBGetter = new SoloButtonSettingGetter(ButtonTypes.BUTTON_B,
        ButtonTypes.BUTTON_EVENT_PRESS);

    private final SoloGoproRequestState goproStateGetter = new SoloGoproRequestState();

    private final AtomicReference<SoloButtonSetting> loadedPresetButtonA = new AtomicReference<>();
    private final AtomicReference<SoloButtonSetting> loadedPresetButtonB = new AtomicReference<>();

    private final AtomicReference<String> vehicleVersion = new AtomicReference<>("");
    private final AtomicReference<String> pixhawkVersion = new AtomicReference<>("");
    private final AtomicReference<String> gimbalVersion = new AtomicReference<>("");

    private final Runnable soloLinkVersionRetriever = new Runnable() {
        @Override
        public void run() {
            final String version = retrieveVersion(SOLO_VERSION_FILENAME);
            if (version != null) {
                vehicleVersion.set(version);
            }

            if(linkListener != null && areVersionsSet())
                linkListener.onVersionsUpdated();
        }
    };

    private final Runnable pixhawkVersionRetriever = new Runnable() {
        @Override
        public void run() {
            final String version = retrieveVersion(PIXHAWK_VERSION_FILENAME);
            if (version != null) {
                pixhawkVersion.set(version);
            }

            if(linkListener != null && areVersionsSet())
                linkListener.onVersionsUpdated();
        }
    };

    private final Runnable gimbalVersionRetriever = new Runnable() {
        @Override
        public void run() {
            final String version = retrieveVersion(GIMBAL_VERSION_FILENAME);
            if(version != null)
                gimbalVersion.set(version);

            if(linkListener != null && areVersionsSet())
                linkListener.onVersionsUpdated();
        }
    };

    private SoloLinkListener linkListener;

    public SoloLinkManager(Context context, Handler handler, ExecutorService asyncExecutor,
                           DataLink.DataLinkProvider mavClient) {
        super(context, new TcpConnection(handler, getSoloLinkIp(), SOLO_LINK_TCP_PORT), handler,
            asyncExecutor,
            mavClient);

        sshLink  = new SshConnection(getSoloLinkIp(), SoloComp.SSH_USERNAME, SoloComp.SSH_PASSWORD, mavClient);
        UdpConnection dataConn = null;
        try {
            dataConn = new UdpConnection(handler, getSoloLinkIp(), SHOT_FOLLOW_UDP_PORT, 14557);
        } catch (UnknownHostException e) {
            Timber.e(e, "Error while creating follow udp connection.");
        }

        followDataConn = dataConn;

    }

    public static String getSoloLinkIp() {
        if (!BuildConfig.SITL_DEBUG) {
            return SOLO_LINK_IP;
        } else {
            return BuildConfig.SOLO_LINK_IP;
        }
    }

    public String getVehicleVersion() {
        return vehicleVersion.get();
    }

    public String getPixhawkVersion() {
        return pixhawkVersion.get();
    }

    public String getGimbalVersion(){
        return gimbalVersion.get();
    }

    public boolean areVersionsSet(){
        return !TextUtils.isEmpty(vehicleVersion.get()) && !TextUtils.isEmpty(pixhawkVersion.get());
    }

    @Override
    public void start(SoloLinkListener listener) {
        if(!isStarted()) {
            Timber.i("Starting solo link manager");
        }

        super.start(listener);
        this.linkListener = listener;
    }

    @Override
    public void stop() {
        if(isStarted()) {
            Timber.i("Stopping solo link manager");
        }

        super.stop();
    }

    @Override
    public void refreshState(){
        Timber.d("Connected to sololink.");

        //Load the mac address for the vehicle.
        loadMacAddress();

        loadPresetButtonSettings();
        loadGoproState();

        refreshSoloLinkVersions();
    }

    @Override
    protected SshConnection getSshLink() {
        return sshLink;
    }

    @Override
    public void onIpDisconnected() {
        Timber.d("Disconnected from sololink.");
        super.onIpDisconnected();
    }

    @Override
    public void onPacketReceived(ByteBuffer packetBuffer) {
        final List<TLVPacket> tlvMsgs = TLVMessageParser.parseTLVPacket(packetBuffer);
        if (tlvMsgs.isEmpty()) {
            return;
        }

        for(TLVPacket tlvMsg : tlvMsgs) {
            final int messageType = tlvMsg.getMessageType();
            Timber.d("Received tlv message: " + messageType);

            //Have shot manager examine the received message first.
            switch (messageType) {
                case TLVMessageTypes.TYPE_SOLO_MESSAGE_SHOT_MANAGER_ERROR:
                    Timber.w(((SoloMessageShotManagerError) tlvMsg).getExceptionInfo());
                    break;

                case TLVMessageTypes.TYPE_SOLO_GET_BUTTON_SETTING:
                    final SoloButtonSettingGetter receivedPresetButton = (SoloButtonSettingGetter) tlvMsg;
                    handleReceivedPresetButton(receivedPresetButton);
                    break;
            }

            if (linkListener != null) {
                linkListener.onTlvPacketReceived(tlvMsg);
            }
        }
    }

    private void sendPacket(byte[] payload, int payloadSize, ICommandListener listener) {
        linkConn.sendPacket(payload, payloadSize, listener);
    }

    private void sendFollowPacket(byte[] payload, int payloadSize, ICommandListener listener) {
        if (followDataConn == null) {
            throw new IllegalStateException("Unable to send follow data.");
        }

        followDataConn.sendPacket(payload, payloadSize, listener);
    }

    public void sendTLVPacket(TLVPacket packet, ICommandListener listener) {
        sendTLVPacket(packet, false, listener);
    }

    public void sendTLVPacket(TLVPacket packet, boolean useFollowLink, ICommandListener listener) {
        if (packet == null) {
            return;
        }

        final byte[] messagePayload = packet.toBytes();
        if (useFollowLink) {
            sendFollowPacket(messagePayload, messagePayload.length, listener);
        } else {
            sendPacket(messagePayload, messagePayload.length, listener);
        }
    }

    public void loadPresetButtonSettings() {
        sendTLVPacket(presetButtonAGetter, new SimpleCommandListener() {
            @Override
            public void onSuccess() {
                sendTLVPacket(presetButtonBGetter, null);
            }
        });
    }

    private void loadGoproState() {
        sendTLVPacket(goproStateGetter, null);
    }

    private void handleReceivedPresetButton(SoloButtonSetting presetButton) {
        final int buttonType = presetButton.getButton();
        switch (buttonType) {
            case ButtonTypes.BUTTON_A:
                loadedPresetButtonA.set(presetButton);
                if (linkListener != null) {
                    linkListener.onPresetButtonLoaded(buttonType, presetButton);
                }
                break;

            case ButtonTypes.BUTTON_B:
                loadedPresetButtonB.set(presetButton);
                if (linkListener != null) {
                    linkListener.onPresetButtonLoaded(buttonType, presetButton);
                }
                break;
        }
    }

    public SoloButtonSetting getLoadedPresetButton(int buttonType) {
        switch (buttonType) {
            case ButtonTypes.BUTTON_A:
                return loadedPresetButtonA.get();

            case ButtonTypes.BUTTON_B:
                return loadedPresetButtonB.get();

            default:
                return null;
        }
    }

    /**
     * Update the vehicle preset button settings
     */
    public void pushPresetButtonSettings(final SoloButtonSettingSetter buttonSetter, final ICommandListener listener) {
        if (!isLinkConnected() || buttonSetter == null) {
            return;
        }

        sendTLVPacket(buttonSetter, new SimpleCommandListener() {
            @Override
            public void onSuccess() {
                postSuccessEvent(listener);
                handleReceivedPresetButton(buttonSetter);
            }

            @Override
            public void onTimeout() {
                postTimeoutEvent(listener);
            }
        });
    }

    public void disableFollowDataConnection() {
        if (followDataConn != null) {
            followDataConn.disconnect();
        }
    }

    public void enableFollowDataConnection() {
        if (followDataConn != null) {
            followDataConn.connect(linkProvider.getConnectionExtras());
        }
    }

    public boolean updateSololinkWifi(CharSequence wifiSsid, CharSequence password) {
        Timber.d(String.format(Locale.US, "Updating solo wifi ssid to %s with password %s", wifiSsid, password));
        try {
            String ssidUpdateResult = sshLink.execute(ControllerLinkManager.SOLOLINK_SSID_CONFIG_PATH + " --set-wifi-ssid " +
                wifiSsid);
            String passwordUpdateResult = sshLink.execute(ControllerLinkManager.SOLOLINK_SSID_CONFIG_PATH + " --set-wifi-password " +
                password);
            String restartResult = sshLink.execute(ControllerLinkManager.SOLOLINK_SSID_CONFIG_PATH + " --reboot");
            return true;
        } catch (IOException e) {
            Timber.e(e, "Error occurred while updating the sololink wifi ssid.");
            return false;
        }
    }

    private void updateSoloLinkVersion() {
        postAsyncTask(soloLinkVersionRetriever);
    }

    private void updatePixhawkVersion() {
        postAsyncTask(pixhawkVersionRetriever);
    }

    private void updateGimbalVersion(){
        postAsyncTask(gimbalVersionRetriever);
    }

    private String retrieveVersion(String versionFile) {
        try {
            String version = sshLink.execute("cat " + versionFile);
            if (TextUtils.isEmpty(version)) {
                Timber.d("No version file was found");
                return "";
            } else {
                return version.split("\n")[0];
            }
        } catch (IOException e) {
            Timber.e("Unable to retrieve the current version.", e);
        }

        return null;
    }

    /**
     * Refresh the vehicle's components versions
     */
    public void refreshSoloLinkVersions() {
        updateSoloLinkVersion();
        updatePixhawkVersion();
        updateGimbalVersion();
    }

}
