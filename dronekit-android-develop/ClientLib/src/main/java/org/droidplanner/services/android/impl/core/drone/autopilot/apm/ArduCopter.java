package org.droidplanner.services.android.impl.core.drone.autopilot.apm;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.MAVLink.Messages.MAVLinkMessage;
import com.github.zafarkhaja.semver.Version;
import com.o3dr.android.client.apis.CapabilityApi;
import com.o3dr.services.android.lib.drone.action.ControlActions;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.model.ICommandListener;

import org.droidplanner.services.android.impl.communication.model.DataLink;
import org.droidplanner.services.android.impl.core.MAVLink.MavLinkCommands;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces;
import org.droidplanner.services.android.impl.core.drone.DroneManager;
import org.droidplanner.services.android.impl.core.drone.LogMessageListener;
import org.droidplanner.services.android.impl.core.drone.profiles.ParameterManager;
import org.droidplanner.services.android.impl.core.drone.variables.ApmModes;
import org.droidplanner.services.android.impl.core.drone.variables.State;
import org.droidplanner.services.android.impl.core.firmware.FirmwareType;
import org.droidplanner.services.android.impl.core.model.AutopilotWarningParser;
import org.droidplanner.services.android.impl.utils.CommonApiUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Fredia Huya-Kouadio on 7/27/15.
 */
public class ArduCopter extends ArduPilot {
    private static final Version ARDU_COPTER_V3_3 = Version.forIntegers(3,3,0);
    private static final Version ARDU_COPTER_V3_4 = Version.forIntegers(3,4,0);

    private static final Version BRAKE_FEATURE_FIRMWARE_VERSION = ARDU_COPTER_V3_3;
    private static final Version COMPASS_CALIBRATION_MIN_VERSION = ARDU_COPTER_V3_4;

    private final ConcurrentHashMap<String, ICommandListener> manualControlStateListeners = new ConcurrentHashMap<>();

    public ArduCopter(String droneId, Context context, DataLink.DataLinkProvider<MAVLinkMessage> mavClient, Handler handler, AutopilotWarningParser warningParser, LogMessageListener logListener) {
        super(droneId, context, mavClient, handler, warningParser, logListener);
    }

    @Override
    public FirmwareType getFirmwareType() {
        return FirmwareType.ARDU_COPTER;
    }

    @Override
    protected boolean setVelocity(Bundle data, ICommandListener listener){
        //Retrieve the normalized values
        float normalizedXVel = data.getFloat(ControlActions.EXTRA_VELOCITY_X);
        float normalizedYVel = data.getFloat(ControlActions.EXTRA_VELOCITY_Y);
        float normalizedZVel = data.getFloat(ControlActions.EXTRA_VELOCITY_Z);

        double attitudeInRad = Math.toRadians(attitude.getYaw());

        final double cosAttitude = Math.cos(attitudeInRad);
        final double sinAttitude = Math.sin(attitudeInRad);

        float projectedX = (float) (normalizedXVel * cosAttitude) - (float) (normalizedYVel * sinAttitude);
        float projectedY = (float) (normalizedXVel * sinAttitude) + (float) (normalizedYVel * cosAttitude);

        //Retrieve the speed parameters.
        float defaultSpeed = 5; //m/s

        ParameterManager parameterManager = getParameterManager();

        //Retrieve the horizontal speed value
        Parameter horizSpeedParam = parameterManager.getParameter("WPNAV_SPEED");
        double horizontalSpeed = horizSpeedParam == null ? defaultSpeed : horizSpeedParam.getValue() / 100;

        //Retrieve the vertical speed value.
        String vertSpeedParamName = normalizedZVel >= 0 ? "WPNAV_SPEED_UP" : "WPNAV_SPEED_DN";
        Parameter vertSpeedParam = parameterManager.getParameter(vertSpeedParamName);
        double verticalSpeed = vertSpeedParam == null ? defaultSpeed : vertSpeedParam.getValue() / 100;

        MavLinkCommands.setVelocityInLocalFrame(this, (float) (projectedX * horizontalSpeed),
                (float) (projectedY * horizontalSpeed),
                (float) (normalizedZVel * verticalSpeed),
                listener);
        return true;
    }

    @Override
    public void destroy(){
        super.destroy();
        manualControlStateListeners.clear();
    }

    @Override
    protected boolean enableManualControl(Bundle data, ICommandListener listener){
        boolean enable = data.getBoolean(ControlActions.EXTRA_DO_ENABLE);
        String appId = data.getString(DroneManager.EXTRA_CLIENT_APP_ID);

        State state = getState();
        ApmModes vehicleMode = state.getMode();
        if(enable){
            if(vehicleMode == ApmModes.ROTOR_GUIDED){
                CommonApiUtils.postSuccessEvent(listener);
            }
            else{
                state.changeFlightMode(ApmModes.ROTOR_GUIDED, listener);
            }

            if(listener != null) {
                manualControlStateListeners.put(appId, listener);
            }
        }
        else{
            manualControlStateListeners.remove(appId);

            if(vehicleMode != ApmModes.ROTOR_GUIDED){
                CommonApiUtils.postSuccessEvent(listener);
            }
            else{
                state.changeFlightMode(ApmModes.ROTOR_LOITER, listener);
            }
        }

        return true;
    }

    @Override
    public void notifyDroneEvent(DroneInterfaces.DroneEventsType event){
        switch(event){
            case MODE:
                //Listen for vehicle mode updates, and update the manual control state listeners appropriately
                ApmModes currentMode = getState().getMode();
                for(ICommandListener listener: manualControlStateListeners.values()) {
                    if (currentMode == ApmModes.ROTOR_GUIDED) {
                        CommonApiUtils.postSuccessEvent(listener);
                    } else {
                        CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                    }
                }
                break;
        }

        super.notifyDroneEvent(event);
    }

    @Override
    protected boolean isFeatureSupported(String featureId){
        switch(featureId){

            case CapabilityApi.FeatureIds.KILL_SWITCH:
                return CommonApiUtils.isKillSwitchSupported(this);

            case CapabilityApi.FeatureIds.COMPASS_CALIBRATION:
                return getFirmwareVersionNumber().greaterThanOrEqualTo(COMPASS_CALIBRATION_MIN_VERSION);

            default:
                return super.isFeatureSupported(featureId);
        }
    }

    @Override
    protected boolean brakeVehicle(ICommandListener listener) {
        if (getFirmwareVersionNumber().greaterThanOrEqualTo(BRAKE_FEATURE_FIRMWARE_VERSION)) {
            getState().changeFlightMode(ApmModes.ROTOR_BRAKE, listener);
        } else {
            super.brakeVehicle(listener);
        }

        return true;
    }
}
