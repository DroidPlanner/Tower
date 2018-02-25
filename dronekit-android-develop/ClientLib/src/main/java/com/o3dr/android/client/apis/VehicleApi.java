package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.action.ConnectionActions.ACTION_CONNECT;
import static com.o3dr.services.android.lib.drone.action.ConnectionActions.ACTION_DISCONNECT;
import static com.o3dr.services.android.lib.drone.action.ConnectionActions.EXTRA_CONNECT_PARAMETER;
import static com.o3dr.services.android.lib.drone.action.ParameterActions.ACTION_REFRESH_PARAMETERS;
import static com.o3dr.services.android.lib.drone.action.ParameterActions.ACTION_WRITE_PARAMETERS;
import static com.o3dr.services.android.lib.drone.action.ParameterActions.EXTRA_PARAMETERS;
import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_ARM;
import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_ENABLE_RETURN_TO_ME;
import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_SET_VEHICLE_HOME;
import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_SET_VEHICLE_MODE;
import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_UPDATE_VEHICLE_DATA_STREAM_RATE;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_ARM;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_EMERGENCY_DISARM;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_IS_RETURN_TO_ME_ENABLED;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_VEHICLE_DATA_STREAM_RATE;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_VEHICLE_HOME_LOCATION;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_VEHICLE_MODE;

/**
 * Provides access to the vehicle specific functionality.
 */
public class VehicleApi extends Api {

    private static final ConcurrentHashMap<Drone, VehicleApi> vehicleApiCache = new ConcurrentHashMap<>();
    private static final Builder<VehicleApi> apiBuilder = new Builder<VehicleApi>() {
        @Override
        public VehicleApi build(Drone drone) {
            return new VehicleApi(drone);
        }
    };

    /**
     * Retrieves a vehicle api instance.
     *
     * @param drone target vehicle
     * @return a VehicleApi instance.
     */
    public static VehicleApi getApi(final Drone drone) {
        return getApi(drone, vehicleApiCache, apiBuilder);
    }

    private final Drone drone;

    private VehicleApi(Drone drone) {
        this.drone = drone;
    }

    /**
     * Establish connection with the vehicle.
     *
     * @param parameter parameter for the connection.
     */
    public void connect(ConnectionParameter parameter) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_CONNECT_PARAMETER, parameter);
        Action connectAction = new Action(ACTION_CONNECT, params);
        drone.performAsyncAction(connectAction);
    }

    /**
     * Break connection with the vehicle.
     */
    public void disconnect() {
        drone.performAsyncAction(new Action(ACTION_DISCONNECT));
    }

    /**
     * Arm or disarm the connected drone.
     *
     * @param arm true to arm, false to disarm.
     */
    public void arm(boolean arm) {
        arm(arm, null);
    }

    /**
     * Arm or disarm the connected drone.
     *
     * @param arm      true to arm, false to disarm.
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void arm(boolean arm, AbstractCommandListener listener) {
        arm(arm, false, listener);
    }

    /**
     * Arm or disarm the connected drone.
     *
     * @param arm             true to arm, false to disarm.
     * @param emergencyDisarm true to skip landing check and disarm immediately,
     *                        false to disarm only if it is safe to do so.
     * @param listener        Register a callback to receive update of the command execution state.
     */
    public void arm(boolean arm, boolean emergencyDisarm, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_ARM, arm);
        params.putBoolean(EXTRA_EMERGENCY_DISARM, emergencyDisarm);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_ARM, params), listener);
    }

    /**
     * Change the vehicle mode for the connected drone.
     *
     * @param newMode new vehicle mode.
     */
    public void setVehicleMode(VehicleMode newMode) {
        setVehicleMode(newMode, null);
    }

    /**
     * Change the vehicle mode for the connected drone.
     *
     * @param newMode  new vehicle mode.
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void setVehicleMode(VehicleMode newMode, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_VEHICLE_MODE, newMode);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_VEHICLE_MODE, params), listener);
    }

    /**
     * Generate action used to refresh the parameters for the connected drone.
     */
    public void refreshParameters() {
        drone.performAsyncAction(new Action(ACTION_REFRESH_PARAMETERS));
    }

    /**
     * Generate action used to write the given parameters to the connected drone.
     *
     * @param parameters parameters to write to the drone.
     * @return
     */
    public void writeParameters(Parameters parameters) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_PARAMETERS, parameters);
        drone.performAsyncAction(new Action(ACTION_WRITE_PARAMETERS, params));
    }

    /**
     * Changes the vehicle home location.
     *
     * @param homeLocation New home coordinate
     * @param listener     Register a callback to receive update of the command execution state.
     */
    public void setVehicleHome(final LatLongAlt homeLocation, final AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_VEHICLE_HOME_LOCATION, homeLocation);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_VEHICLE_HOME, params), listener);
    }

    /**
     * Enables 'return to me'
     * @param isEnabled
     * @param listener
     */
    public void enableReturnToMe(boolean isEnabled, final AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_IS_RETURN_TO_ME_ENABLED, isEnabled);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_ENABLE_RETURN_TO_ME, params), listener);
    }

    /**
     * Update the vehicle data stream rate.
     *
     * Note: This is ineffective for Solo vehicles since their data stream rate is handled
     * by the onboard companion computer.
     *
     * @param rate          The new data stream rate
     * @param listener      Register a callback to receive update of the command execution state
     * @since 2.9.0
     */
    public void updateVehicleDataStreamRate(int rate, final AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putInt(EXTRA_VEHICLE_DATA_STREAM_RATE, rate);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_UPDATE_VEHICLE_DATA_STREAM_RATE, params), listener);
    }
}
