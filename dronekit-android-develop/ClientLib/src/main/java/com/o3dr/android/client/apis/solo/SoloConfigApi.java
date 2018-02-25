package com.o3dr.android.client.apis.solo;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.utils.TxPowerComplianceCountries;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerMode;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerUnits;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSettingSetter;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.companion.solo.action.SoloConfigActions.ACTION_REFRESH_SOLO_VERSIONS;
import static com.o3dr.services.android.lib.drone.companion.solo.action.SoloConfigActions.ACTION_UPDATE_BUTTON_SETTINGS;
import static com.o3dr.services.android.lib.drone.companion.solo.action.SoloConfigActions.ACTION_UPDATE_CONTROLLER_MODE;
import static com.o3dr.services.android.lib.drone.companion.solo.action.SoloConfigActions.ACTION_UPDATE_CONTROLLER_UNIT;
import static com.o3dr.services.android.lib.drone.companion.solo.action.SoloConfigActions.ACTION_UPDATE_TX_POWER_COMPLIANCE_COUNTRY;
import static com.o3dr.services.android.lib.drone.companion.solo.action.SoloConfigActions.ACTION_UPDATE_WIFI_SETTINGS;
import static com.o3dr.services.android.lib.drone.companion.solo.action.SoloConfigActions.EXTRA_BUTTON_SETTINGS;
import static com.o3dr.services.android.lib.drone.companion.solo.action.SoloConfigActions.EXTRA_CONTROLLER_MODE;
import static com.o3dr.services.android.lib.drone.companion.solo.action.SoloConfigActions.EXTRA_CONTROLLER_UNIT;
import static com.o3dr.services.android.lib.drone.companion.solo.action.SoloConfigActions.EXTRA_TX_POWER_COMPLIANT_COUNTRY_CODE;
import static com.o3dr.services.android.lib.drone.companion.solo.action.SoloConfigActions.EXTRA_WIFI_PASSWORD;
import static com.o3dr.services.android.lib.drone.companion.solo.action.SoloConfigActions.EXTRA_WIFI_SSID;

/**
 * Created by Fredia Huya-Kouadio on 7/31/15.
 */
public class SoloConfigApi extends SoloApi {

    private static final ConcurrentHashMap<Drone, SoloConfigApi> soloConfigApiCache = new ConcurrentHashMap<>();
    private static final Builder<SoloConfigApi> apiBuilder = new Builder<SoloConfigApi>() {
        @Override
        public SoloConfigApi build(Drone drone) {
            return new SoloConfigApi(drone);
        }
    };

    /**
     * Retrieves a sololink api instance.
     *
     * @param drone target vehicle
     * @return a SoloLinkApi instance.
     */
    public static SoloConfigApi getApi(final Drone drone) {
        return getApi(drone, soloConfigApiCache, apiBuilder);
    }

    protected SoloConfigApi(Drone drone) {
        super(drone);
    }

    /**
     * Updates the wifi settings for the solo vehicle.
     *
     * @param wifiSsid     Updated wifi ssid
     * @param wifiPassword Updated wifi password
     * @param listener     Register a callback to receive update of the command execution status.
     */
    public void updateWifiSettings(String wifiSsid, String wifiPassword, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putString(EXTRA_WIFI_SSID, wifiSsid);
        params.putString(EXTRA_WIFI_PASSWORD, wifiPassword);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_UPDATE_WIFI_SETTINGS, params), listener);
    }

    /**
     * Updates the button settings for the solo vehicle.
     *
     * @param buttonSettings Updated button settings.
     * @param listener       Register a callback to receive update of the command execution status.
     */
    public void updateButtonSettings(SoloButtonSettingSetter buttonSettings, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_BUTTON_SETTINGS, buttonSettings);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_UPDATE_BUTTON_SETTINGS, params), listener);
    }

    /**
     * Updates the controller mode (joystick mapping)
     *
     * @param controllerMode Controller mode. @see {@link com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerMode.ControllerMode}
     * @param listener       Register a callback to receive update of the command execution status.
     */
    public void updateControllerMode(@SoloControllerMode.ControllerMode int controllerMode, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putInt(EXTRA_CONTROLLER_MODE, controllerMode);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_UPDATE_CONTROLLER_MODE, params), listener);
    }

    /**
     * Updates the tx power compliance to the specified country.
     *
     * @param compliantCountry Country code which the controller will be compliant.
     * @param listener    Register a callback to receive update of the command execution status.
     */
    public void updateTxPowerComplianceCountry(TxPowerComplianceCountries compliantCountry, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putString(EXTRA_TX_POWER_COMPLIANT_COUNTRY_CODE, compliantCountry.name());
        drone.performAsyncActionOnDroneThread(new Action(ACTION_UPDATE_TX_POWER_COMPLIANCE_COUNTRY, params), listener);
    }

    /**
     * Refresh the solo versions info.
     */
    public void refreshSoloVersions(){
        drone.performAsyncActionOnDroneThread(new Action(ACTION_REFRESH_SOLO_VERSIONS), null);
    }

    /**
     *  Updates the controller unit system.
     * @param controllerUnit Controller unit system. @see {@link com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerUnits.ControllerUnit}
     * @param listener Register a callback that receive update of the command execution status.
     */
    public void updateControllerUnit(@SoloControllerUnits.ControllerUnit String controllerUnit, final AbstractCommandListener listener){
        if(SoloControllerUnits.UNKNOWN.equals(controllerUnit)){
            if(listener != null) {
                drone.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onError(CommandExecutionError.COMMAND_DENIED);
                    }
                });
            }
            return;
        }

        Bundle params = new Bundle();
        params.putString(EXTRA_CONTROLLER_UNIT, controllerUnit);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_UPDATE_CONTROLLER_UNIT, params), listener);
    }
}
