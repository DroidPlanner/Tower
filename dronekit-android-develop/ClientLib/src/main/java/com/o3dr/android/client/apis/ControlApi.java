package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_DO_GUIDED_TAKEOFF;
import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_ENABLE_MANUAL_CONTROL;
import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_LOOK_AT_TARGET;
import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_SEND_BRAKE_VEHICLE;
import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_SEND_GUIDED_POINT;
import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_SET_CONDITION_YAW;
import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_SET_GUIDED_ALTITUDE;
import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_SET_VELOCITY;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_ALTITUDE;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_DO_ENABLE;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_FORCE_GUIDED_POINT;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_GUIDED_POINT;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_LOOK_AT_TARGET;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_VELOCITY_X;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_VELOCITY_Y;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_VELOCITY_Z;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_YAW_CHANGE_RATE;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_YAW_IS_RELATIVE;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_YAW_TARGET_ANGLE;

/**
 * Provides access to the vehicle control functionality.
 * <p/>
 * Use of this api might required the vehicle to be in a specific flight mode (i.e: GUIDED)
 * <p/>
 * Created by Fredia Huya-Kouadio on 9/7/15.
 */
public class ControlApi extends Api {

    private static final ConcurrentHashMap<Drone, ControlApi> apiCache = new ConcurrentHashMap<>();
    private static final Builder<ControlApi> apiBuilder = new Builder<ControlApi>() {
        @Override
        public ControlApi build(Drone drone) {
            return new ControlApi(drone);
        }
    };

    /**
     * Retrieves a control api instance.
     *
     * @param drone
     * @return
     */
    public static ControlApi getApi(final Drone drone) {
        return getApi(drone, apiCache, apiBuilder);
    }

    private final Drone drone;

    private ControlApi(Drone drone) {
        this.drone = drone;
    }

    /**
     * Perform a guided take off.
     *
     * @param altitude altitude in meters
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void takeoff(double altitude, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putDouble(EXTRA_ALTITUDE, altitude);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_DO_GUIDED_TAKEOFF, params), listener);
    }

    /**
     * Pause the vehicle at its current location.
     *
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void pauseAtCurrentLocation(final AbstractCommandListener listener) {
        Bundle params = new Bundle();
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SEND_BRAKE_VEHICLE, params), listener);
    }

    /**
     * Instructs the vehicle to go to the specified location.
     *
     * @param point    target location
     * @param force    true to enable guided mode is required.
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void goTo(LatLong point, boolean force, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_FORCE_GUIDED_POINT, force);
        params.putParcelable(EXTRA_GUIDED_POINT, point);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SEND_GUIDED_POINT, params), listener);
    }

    /**
     * Instructs the vehicle to orient toward the specified location
     *
     * @param point
     * @param force
     * @param listener
     * @since 2.9.0
     */
    public void lookAt(LatLongAlt point, boolean force, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_FORCE_GUIDED_POINT, force);
        params.putParcelable(EXTRA_LOOK_AT_TARGET, point);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_LOOK_AT_TARGET, params), listener);
    }

    /**
     * Instructs the vehicle to climb to the specified altitude.
     *
     * @param altitude altitude in meters
     */
    public void climbTo(double altitude) {
        Bundle params = new Bundle();
        params.putDouble(EXTRA_ALTITUDE, altitude);
        drone.performAsyncAction(new Action(ACTION_SET_GUIDED_ALTITUDE, params));
    }

    /**
     * Instructs the vehicle to turn to the specified target angle
     *
     * @param targetAngle Target angle in degrees [0-360], with 0 == north.
     * @param turnRate    Turning rate normalized to the range [-1.0f, 1.0f]. Positive values for clockwise turns, and negative values for counter-clockwise turns.
     * @param isRelative  True is the target angle is relative to the current vehicle attitude, false otherwise if it's absolute.
     * @param listener    Register a callback to receive update of the command execution state.
     */
    public void turnTo(float targetAngle, float turnRate, boolean isRelative, AbstractCommandListener listener) {
        if (!isWithinBounds(targetAngle, 0, 360) || !isWithinBounds(turnRate, -1.0f, 1.0f)) {
            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
            return;
        }

        Bundle params = new Bundle();
        params.putFloat(EXTRA_YAW_TARGET_ANGLE, targetAngle);
        params.putFloat(EXTRA_YAW_CHANGE_RATE, turnRate);
        params.putBoolean(EXTRA_YAW_IS_RELATIVE, isRelative);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_CONDITION_YAW, params), listener);
    }

    /**
     * Move the vehicle along the specified normalized velocity vector.
     *
     * @param vx       x velocity normalized to the range [-1.0f, 1.0f]. Generally correspond to the pitch of the vehicle.
     * @param vy       y velocity normalized to the range [-1.0f, 1.0f]. Generally correspond to the roll of the vehicle.
     * @param vz       z velocity normalized to the range [-1.0f, 1.0f]. Generally correspond to the thrust of the vehicle.
     * @param listener Register a callback to receive update of the command execution state.
     * @since 2.6.9
     */
    public void manualControl(float vx, float vy, float vz, AbstractCommandListener listener) {
        if (!isWithinBounds(vx, -1f, 1f) || !isWithinBounds(vy, -1f, 1f) || !isWithinBounds(vz, -1f, 1f)) {
            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
            return;
        }

        Bundle params = new Bundle();
        params.putFloat(EXTRA_VELOCITY_X, vx);
        params.putFloat(EXTRA_VELOCITY_Y, vy);
        params.putFloat(EXTRA_VELOCITY_Z, vz);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_VELOCITY, params), listener);
    }

    /**
     * [Dis|En]able manual control on the vehicle.
     * The result of the action will be conveyed through the passed listener.
     *
     * @param enable   True to enable manual control, false to disable.
     * @param listener Register a callback to receive the result of the operation.
     * @since 2.6.9
     */
    public void enableManualControl(final boolean enable, final ManualControlStateListener listener) {
        AbstractCommandListener listenerWrapper = listener == null ? null
                : new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                if (enable) {
                    listener.onManualControlToggled(true);
                } else {
                    listener.onManualControlToggled(false);
                }
            }

            @Override
            public void onError(int executionError) {
                if (enable) {
                    listener.onManualControlToggled(false);
                }
            }

            @Override
            public void onTimeout() {
                if (enable) {
                    listener.onManualControlToggled(false);
                }
            }
        };

        Bundle params = new Bundle();
        params.putBoolean(EXTRA_DO_ENABLE, enable);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_ENABLE_MANUAL_CONTROL, params), listenerWrapper);
    }

    private static boolean isWithinBounds(float value, float lowerBound, float upperBound) {
        return value <= upperBound && value >= lowerBound;
    }

    /**
     * Used to monitor the state of manual control for the vehicle.
     *
     * @since 2.6.9
     */
    public interface ManualControlStateListener {
        /**
         * Manual control is toggled on the vehicle.
         * @param isEnabled True if manual control is enabled, false if disabled.
         */
        void onManualControlToggled(boolean isEnabled);
    }
}
