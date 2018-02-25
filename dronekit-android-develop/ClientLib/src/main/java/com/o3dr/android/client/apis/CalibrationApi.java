package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.ACTION_ACCEPT_MAGNETOMETER_CALIBRATION;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.ACTION_CANCEL_MAGNETOMETER_CALIBRATION;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.ACTION_SEND_IMU_CALIBRATION_ACK;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.ACTION_START_IMU_CALIBRATION;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.ACTION_START_MAGNETOMETER_CALIBRATION;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.EXTRA_IMU_STEP;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.EXTRA_RETRY_ON_FAILURE;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.EXTRA_SAVE_AUTOMATICALLY;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.EXTRA_START_DELAY;

/**
 * Provides access to the calibration specific functionality.
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class CalibrationApi extends Api {

    private static final ConcurrentHashMap<Drone, CalibrationApi> calibrationApiCache = new ConcurrentHashMap<>();
    private static final Builder<CalibrationApi> apiBuilder = new Builder<CalibrationApi>() {
        @Override
        public CalibrationApi build(Drone drone) {
            return new CalibrationApi(drone);
        }
    };

    /**
     * Retrieves a CalibrationApi instance.
     *
     * @param drone target vehicle.
     * @return a CalibrationApi instance.
     */
    public static CalibrationApi getApi(final Drone drone) {
        return getApi(drone, calibrationApiCache, apiBuilder);
    }

    private final Drone drone;

    private CalibrationApi(Drone drone) {
        this.drone = drone;
    }

    /**
     * Start the imu calibration.
     */
    public void startIMUCalibration() {
        startIMUCalibration(null);
    }

    /**
     * Start the imu calibration.
     *
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void startIMUCalibration(AbstractCommandListener listener) {
        drone.performAsyncActionOnDroneThread(new Action(ACTION_START_IMU_CALIBRATION), listener);
    }

    /**
     * Generate an action to send an imu calibration acknowledgement.
     */
    public void sendIMUAck(int step) {
        Bundle params = new Bundle();
        params.putInt(EXTRA_IMU_STEP, step);
        drone.performAsyncAction(new Action(ACTION_SEND_IMU_CALIBRATION_ACK, params));
    }

    /**
     * Start the magnetometer calibration process.
     */
    public void startMagnetometerCalibration() {
        startMagnetometerCalibration(false, true, 0);
    }

    /**
     * Start the magnetometer calibration process
     *
     * @param retryOnFailure    if true, automatically retry the magnetometer calibration if it fails
     * @param saveAutomatically if true, save the calibration automatically without user input.
     * @param startDelay        positive delay in seconds before starting the calibration
     */
    public void startMagnetometerCalibration(boolean retryOnFailure, boolean saveAutomatically, int startDelay) {
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_RETRY_ON_FAILURE, retryOnFailure);
        params.putBoolean(EXTRA_SAVE_AUTOMATICALLY, saveAutomatically);
        params.putInt(EXTRA_START_DELAY, startDelay);

        drone.performAsyncAction(new Action(ACTION_START_MAGNETOMETER_CALIBRATION, params));
    }

    /**
     * Confirm the result of the magnetometer calibration.
     */
    public void acceptMagnetometerCalibration() {
        drone.performAsyncAction(new Action(ACTION_ACCEPT_MAGNETOMETER_CALIBRATION));
    }

    /**
     * Cancel the magnetometer calibration is one if running.
     */
    public void cancelMagnetometerCalibration() {
        drone.performAsyncAction(new Action(ACTION_CANCEL_MAGNETOMETER_CALIBRATION));
    }
}
