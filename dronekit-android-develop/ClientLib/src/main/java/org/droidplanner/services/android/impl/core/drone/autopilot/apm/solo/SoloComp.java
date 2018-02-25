package org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Pair;
import android.util.SparseArray;

import com.o3dr.android.client.BuildConfig;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.companion.solo.SoloEventExtras;
import com.o3dr.services.android.lib.drone.companion.solo.SoloEvents;
import com.o3dr.services.android.lib.drone.companion.solo.button.ButtonPacket;
import com.o3dr.services.android.lib.drone.companion.solo.button.ButtonTypes;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerMode;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerUnits;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSetting;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSettingSetter;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproState;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproStateV2;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloMessageLocation;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;
import com.o3dr.services.android.lib.model.ICommandListener;

import org.droidplanner.services.android.impl.communication.model.DataLink;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.controller.ControllerLinkListener;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.controller.ControllerLinkManager;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.sololink.SoloLinkListener;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.sololink.SoloLinkManager;
import org.droidplanner.services.android.impl.utils.NetworkUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

/**
 * Sololink companion computer implementation
 * Created by Fredia Huya-Kouadio on 7/9/15.
 */
public class SoloComp implements SoloLinkListener, ControllerLinkListener {

    public interface SoloCompListener {
        void onConnected();

        void onDisconnected();

        void onTlvPacketReceived(TLVPacket packet);

        void onPresetButtonLoaded(int buttonType, SoloButtonSetting buttonSettings);

        void onWifiInfoUpdated(String wifiName, String wifiPassword);

        void onButtonPacketReceived(ButtonPacket packet);

        void onTxPowerComplianceCountryUpdated(String compliantCountry);

        void onVersionsUpdated();

        void onControllerEvent(String event, Bundle eventInfo);
    }

    public static final String SOLO_LINK_WIFI_PREFIX = "SoloLink_";

    public static final String SSH_USERNAME = "root";
    public static final String SSH_PASSWORD = "TjSDBkAu";

    private final ControllerLinkManager controllerLinkManager;
    private final SoloLinkManager soloLinkMgr;

    private final Context context;
    private final Handler handler;
    private final ExecutorService asyncExecutor;

    private SoloCompListener compListener;

    private SoloGoproState goproState;
    private SoloGoproStateV2 goproStateV2;

    /**
     * Solo companion computer implementation
     *
     * @param context Application context
     */
    public SoloComp(Context context, Handler handler, DataLink.DataLinkProvider mavClient) {
        this.context = context;

        this.handler = handler;
        asyncExecutor = Executors.newCachedThreadPool();

        this.controllerLinkManager = new ControllerLinkManager(context, handler, asyncExecutor, mavClient);
        this.soloLinkMgr = new SoloLinkManager(context, handler, asyncExecutor, mavClient);
    }

    public SoloGoproState getGoproState() {
        return goproState;
    }

    public SoloGoproStateV2 getGoproStateV2(){
        return goproStateV2;
    }

    public boolean hasStreamingPermission(){
        if(BuildConfig.SITL_DEBUG)
            return true;

        return controllerLinkManager.hasStreamingPermission();
    }

    public void setListener(SoloCompListener listener) {
        this.compListener = listener;
    }

    public static boolean isAvailable(Context context) {
        return NetworkUtils.isOnSololinkNetwork(context);
    }

    public void start() {
        if (!isAvailable(context)) {
            return;
        }

        if(!BuildConfig.SITL_DEBUG) {
            controllerLinkManager.start(this);
        }

        soloLinkMgr.start(this);
    }

    public void stop() {
        soloLinkMgr.stop();

        if(!BuildConfig.SITL_DEBUG) {
            controllerLinkManager.stop();
        }
    }

    public void refreshState() {
        soloLinkMgr.refreshState();

        if(!BuildConfig.SITL_DEBUG)
            controllerLinkManager.refreshState();
    }

    /**
     * Terminates and releases resources used by this companion computer instance. The instance should no longer be used after calling this method.
     */
    public void destroy() {
        stop();
        asyncExecutor.shutdownNow();
    }

    @Override
    public void onTlvPacketReceived(TLVPacket packet) {
        if (packet == null)
            return;

        switch (packet.getMessageType()) {
            case TLVMessageTypes.TYPE_SOLO_GOPRO_STATE:
                goproState = (SoloGoproState) packet;
                Timber.d("Updated gopro state.");
                break;

            case TLVMessageTypes.TYPE_SOLO_GOPRO_STATE_V2:
                goproStateV2 = (SoloGoproStateV2) packet;
                Timber.i("Updated gopro state.");
                break;
        }

        if (compListener != null)
            compListener.onTlvPacketReceived(packet);
    }

    @Override
    public void onWifiInfoUpdated(String wifiName, String wifiPassword) {
        if (compListener != null)
            compListener.onWifiInfoUpdated(wifiName, wifiPassword);
    }

    @Override
    public void onButtonPacketReceived(ButtonPacket packet) {
        if (compListener != null)
            compListener.onButtonPacketReceived(packet);
    }

    @Override
    public void onTxPowerComplianceCountryUpdated(String compliantCountry) {
        if (compListener != null)
            compListener.onTxPowerComplianceCountryUpdated(compliantCountry);
    }

    @Override
    public void onControllerModeUpdated() {
        if (compListener != null) {
            final Bundle eventInfo = new Bundle();
            eventInfo.putInt(SoloEventExtras.EXTRA_SOLO_CONTROLLER_MODE, getControllerMode());
            compListener.onControllerEvent(SoloEvents.SOLO_CONTROLLER_MODE_UPDATED, eventInfo);
        }
    }

    @Override
    public void onControllerUnitUpdated(String trimmedResponse) {
        if (compListener != null) {
            final Bundle eventInfo = new Bundle();
            eventInfo.putString(SoloEventExtras.EXTRA_SOLO_CONTROLLER_UNIT, trimmedResponse);
            compListener.onControllerEvent(SoloEvents.SOLO_CONTROLLER_UNIT_UPDATED, eventInfo);
        }
    }

    @Override
    public void onPresetButtonLoaded(int buttonType, SoloButtonSetting buttonSettings) {
        if (compListener != null)
            compListener.onPresetButtonLoaded(buttonType, buttonSettings);
    }

    @Override
    public void onLinkConnected() {
        if (isConnected()) {
            if (compListener != null)
                compListener.onConnected();
        } else {
            if (!controllerLinkManager.isLinkConnected() && !BuildConfig.SITL_DEBUG)
                controllerLinkManager.start(this);

            if (!soloLinkMgr.isLinkConnected())
                soloLinkMgr.start(this);
        }
    }

    @Override
    public void onLinkDisconnected() {
        if (compListener != null)
            compListener.onDisconnected();

        soloLinkMgr.stop();

        if(!BuildConfig.SITL_DEBUG) {
            controllerLinkManager.stop();
        }
    }

    @Override
    public void onVersionsUpdated() {
        if (compListener != null)
            compListener.onVersionsUpdated();
    }

    @Override
    public void onMacAddressUpdated() {
        final String soloMacAddress = soloLinkMgr.getMacAddress();
        final String artooMacAddress = controllerLinkManager.getMacAddress();
        if (!TextUtils.isEmpty(soloMacAddress) && !TextUtils.isEmpty(artooMacAddress) && compListener != null) {
            compListener.onControllerEvent(AttributeEvent.STATE_VEHICLE_UID, null);
        }
    }

    public boolean isConnected() {
        if(BuildConfig.SITL_DEBUG)
            return soloLinkMgr.isLinkConnected();

        return controllerLinkManager.isLinkConnected() && soloLinkMgr.isLinkConnected();
    }

    public Pair<String, String> getWifiSettings() {
        return controllerLinkManager.getSoloLinkWifiInfo();
    }

    public String getTxPowerCompliantCountry() {
        return controllerLinkManager.getTxPowerCompliantCountry();
    }

    public void refreshSoloVersions() {
        soloLinkMgr.refreshSoloLinkVersions();

        if(!BuildConfig.SITL_DEBUG)
            controllerLinkManager.refreshControllerVersions();
    }

    public String getControllerVersion() {
        return controllerLinkManager.getArtooVersion();
    }

    public String getControllerFirmwareVersion() {
        return controllerLinkManager.getStm32Version();
    }

    public String getVehicleVersion() {
        return soloLinkMgr.getVehicleVersion();
    }

    @SoloControllerMode.ControllerMode
    public int getControllerMode() {
        return controllerLinkManager.getControllerMode();
    }

    @SoloControllerUnits.ControllerUnit
    public String getControllerUnit() {
        return controllerLinkManager.getControllerUnit();
    }

    public String getSoloMacAddress() {
        return soloLinkMgr.getMacAddress();
    }

    public String getControllerMacAddress() {
        return controllerLinkManager.getMacAddress();
    }

    public String getAutopilotVersion() {
        return soloLinkMgr.getPixhawkVersion();
    }

    public String getGimbalVersion() {
        return soloLinkMgr.getGimbalVersion();
    }

    public SoloButtonSetting getButtonSetting(int buttonType) {
        return soloLinkMgr.getLoadedPresetButton(buttonType);
    }

    public SparseArray<SoloButtonSetting> getButtonSettings() {
        final SparseArray<SoloButtonSetting> buttonSettings = new SparseArray<>(2);
        buttonSettings.append(ButtonTypes.BUTTON_A, soloLinkMgr.getLoadedPresetButton(ButtonTypes.BUTTON_A));
        buttonSettings.append(ButtonTypes.BUTTON_B, soloLinkMgr.getLoadedPresetButton(ButtonTypes.BUTTON_B));
        return buttonSettings;
    }

    public void sendSoloLinkMessage(TLVPacket message, ICommandListener listener) {
        soloLinkMgr.sendTLVPacket(message, listener);
    }

    public void updateWifiSettings(final String wifiSsid, final String wifiPassword,
                                   final ICommandListener listener) {
        postAsyncTask(new Runnable() {
            @Override
            public void run() {
                if (soloLinkMgr.updateSololinkWifi(wifiSsid, wifiPassword)
                        && controllerLinkManager.updateSololinkWifi(wifiSsid, wifiPassword)) {
                    Timber.d("Sololink wifi update successful.");

                    if (listener != null) {
                        postSuccessEvent(listener);
                    }
                } else {
                    Timber.d("Sololink wifi update failed.");
                    if (listener != null) {
                        postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                    }
                }
            }
        });
    }

    public void pushButtonSettings(SoloButtonSettingSetter buttonSettings, ICommandListener listener) {
        soloLinkMgr.pushPresetButtonSettings(buttonSettings, listener);
    }

    public void updateControllerMode(@SoloControllerMode.ControllerMode final int selectedMode, ICommandListener listener) {
        controllerLinkManager.updateControllerMode(selectedMode, listener);
    }

    public void updateControllerUnit(@SoloControllerUnits.ControllerUnit final String selectedUnit, ICommandListener listener) {
        controllerLinkManager.updateControllerUnit(selectedUnit, listener);
    }

    public void updateTxPowerComplianceCountry(String compliantCountry, ICommandListener listener) {
        controllerLinkManager.setTxPowerComplianceCountry(compliantCountry, listener);
    }

    protected void postAsyncTask(Runnable task) {
        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.execute(task);
        }
    }

    protected void postSuccessEvent(final ICommandListener listener) {
        if (handler != null && listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onSuccess();
                    } catch (RemoteException e) {
                        Timber.e(e, e.getMessage());
                    }
                }
            });
        }
    }

    protected void postTimeoutEvent(final ICommandListener listener) {
        if (handler != null && listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onTimeout();
                    } catch (RemoteException e) {
                        Timber.e(e, e.getMessage());
                    }
                }
            });
        }
    }

    protected void postErrorEvent(final int error, final ICommandListener listener) {
        if (handler != null && listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onError(error);
                    } catch (RemoteException e) {
                        Timber.e(e, e.getMessage());
                    }
                }
            });
        }
    }

    public void enableFollowDataConnection() {
        soloLinkMgr.enableFollowDataConnection();
    }

    public void disableFollowDataConnection() {
        soloLinkMgr.disableFollowDataConnection();
    }

    public void updateFollowCenter(SoloMessageLocation location) {
        soloLinkMgr.sendTLVPacket(location, true, null);
    }

}
