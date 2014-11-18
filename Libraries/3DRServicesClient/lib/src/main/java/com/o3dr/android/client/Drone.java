package com.o3dr.android.client;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.event.Event;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.o3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.FootPrint;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.model.IDroidPlannerApi;
import com.o3dr.services.android.lib.model.IDroidPlannerApiCallback;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fhuya on 11/4/14.
 */
public class Drone {

    private static final String CLAZZ_NAME = Drone.class.getName();
    private static final String TAG = Drone.class.getSimpleName();

    public static final int COLLISION_SECONDS_BEFORE_COLLISION = 2;
    public static final double COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND = -3.0;
    public static final double COLLISION_SAFE_ALTITUDE_METERS = 1.0;

    public static final String ACTION_GROUND_COLLISION_IMMINENT = CLAZZ_NAME +
            ".ACTION_GROUND_COLLISION_IMMINENT";
    public static final String EXTRA_IS_GROUND_COLLISION_IMMINENT =
            "extra_is_ground_collision_imminent";

    private final ConcurrentLinkedQueue<DroneListener> droneListeners = new
            ConcurrentLinkedQueue<DroneListener>();

    private final Handler handler;
    private final ServiceManager serviceMgr;
    private final DroneCallback droneCallback;
    private IDroidPlannerApi dpApi;

    private ConnectionParameter connectionParameter;

    // flightTimer
    // ----------------
    private long startTime = 0;
    private long elapsedFlightTime = 0;
    private AtomicBoolean isTimerRunning = new AtomicBoolean(false);

    public Drone(ServiceManager serviceManager, Handler handler) {
        this.handler = handler;
        this.serviceMgr = serviceManager;
        this.droneCallback = new DroneCallback(this);
    }

    public void start() {
        if (!serviceMgr.isServiceConnected())
            throw new IllegalStateException("Service manager must be connected.");

        try {
            this.dpApi = serviceMgr.get3drServices().acquireDroidPlannerApi();
        } catch (RemoteException e) {
            throw new IllegalStateException("Unable to retrieve a valid drone handle.");
        }

        requestEventUpdates(this.droneCallback);
        resetFlightTimer();
    }

    public void destroy() {
        removeEventUpdates(this.droneCallback);

        try {
            if (serviceMgr.isServiceConnected())
                serviceMgr.get3drServices().releaseDroidPlannerApi(this.dpApi);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        this.dpApi = null;
        droneListeners.clear();
    }

    private void checkForGroundCollision() {
        Speed speed = getSpeed();
        Altitude altitude = getAltitude();
        if (speed == null || altitude == null)
            return;

        double verticalSpeed = speed.getVerticalSpeed();
        double altitudeValue = altitude.getAltitude();

        boolean isCollisionImminent = altitudeValue
                + (verticalSpeed * COLLISION_SECONDS_BEFORE_COLLISION) < 0
                && verticalSpeed < COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND
                && altitudeValue > COLLISION_SAFE_ALTITUDE_METERS;

        Bundle extrasBundle = new Bundle(1);
        extrasBundle.putBoolean(EXTRA_IS_GROUND_COLLISION_IMMINENT, isCollisionImminent);
        notifyDroneEvent(ACTION_GROUND_COLLISION_IMMINENT, extrasBundle);
    }

    private void handleRemoteException(RemoteException e) {
        final String errorMsg = e.getMessage();
        Log.e(TAG, errorMsg, e);
        notifyDroneServiceInterrupted(errorMsg);
    }

    public double getSpeedParameter() {
        Parameters params = getParameters();
        if (params != null) {
            Parameter speedParam = params.getParameter("WPNAV_SPEED");
            if (speedParam != null)
                return speedParam.getValue();
        }

        return 0;
    }

    public void resetFlightTimer() {
        elapsedFlightTime = 0;
        startTime = SystemClock.elapsedRealtime();
        isTimerRunning.set(true);
    }

    public void startTimer() {
        if (isTimerRunning.compareAndSet(false, true))
            startTime = SystemClock.elapsedRealtime();
    }

    public void stopTimer() {
        if (isTimerRunning.compareAndSet(true, false)) {
            // lets calc the final elapsed timer
            elapsedFlightTime += SystemClock.elapsedRealtime() - startTime;
            startTime = SystemClock.elapsedRealtime();
        }
    }

    public long getFlightTime() {
        State droneState = getState();
        if (droneState != null && droneState.isFlying()) {
            // calc delta time since last checked
            elapsedFlightTime += SystemClock.elapsedRealtime() - startTime;
            startTime = SystemClock.elapsedRealtime();
        }
        return elapsedFlightTime / 1000;
    }


    public Gps getGps() {
        if (isApiValid()) {
            try {
                return dpApi.getGps();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }

        return new Gps();
    }


    public State getState() {
        if (isApiValid()) {
            try {
                return dpApi.getState();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }

        return new State();
    }


    public VehicleMode[] getAllVehicleModes() {
        if (isApiValid()) {
            try {
                return dpApi.getAllVehicleModes();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new VehicleMode[0];
    }


    public Parameters getParameters() {
        if (isApiValid()) {
            try {
                return dpApi.getParameters();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new Parameters();
    }


    public Speed getSpeed() {
        if (isApiValid()) {
            try {
                return dpApi.getSpeed();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new Speed();
    }


    public Attitude getAttitude() {
        if (isApiValid()) {
            try {
                return dpApi.getAttitude();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new Attitude();
    }


    public Home getHome() {
        if (isApiValid()) {
            try {
                return dpApi.getHome();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new Home();
    }


    public Battery getBattery() {
        if (isApiValid()) {
            try {
                return dpApi.getBattery();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new Battery();
    }


    public Altitude getAltitude() {
        if (isApiValid()) {
            try {
                return dpApi.getAltitude();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new Altitude();
    }


    public Mission getMission() {
        if (isApiValid()) {
            try {
                return dpApi.getMission();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new Mission();
    }


    public Signal getSignal() {
        if (isApiValid()) {
            try {
                return dpApi.getSignal();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }

        return new Signal();
    }


    public Type getType() {
        if (isApiValid()) {
            try {
                return dpApi.getType();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new Type();
    }

    public void connect(final ConnectionParameter connParams) {
        if (isApiValid()) {
            try {
                dpApi.connect(connParams);
                this.connectionParameter = connParams;
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void disconnect() {
        if (isApiValid()) {
            try {
                dpApi.disconnect();
                this.connectionParameter = null;
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    private boolean isApiValid() {
        return dpApi != null;
    }


    public boolean isConnected() {
        try {
            return isApiValid() && dpApi.isConnected();
        } catch (RemoteException e) {
            handleRemoteException(e);
        }
        return false;
    }


    public GuidedState getGuidedState() {
        if (isApiValid()) {
            try {
                return dpApi.getGuidedState();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new GuidedState();
    }


    public FollowState getFollowState() {
        if (isApiValid()) {
            try {
                return dpApi.getFollowState();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new FollowState();
    }


    public FollowType[] getFollowTypes() {
        if (isApiValid()) {
            try {
                return dpApi.getFollowTypes();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new FollowType[0];
    }


    public CameraDetail[] getCameraDetails() {
        if (isApiValid()) {
            try {
                return dpApi.getCameraDetails();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new CameraDetail[0];
    }


    public FootPrint getLastCameraFootPrint() {
        if (isApiValid()) {
            try {
                return dpApi.getLastCameraFootPrint();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new FootPrint();
    }


    public FootPrint[] getCameraFootPrints() {
        if (isApiValid()) {
            try {
                return dpApi.getCameraFootPrints();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new FootPrint[0];
    }

    public ConnectionParameter getConnectionParameter() {
        return this.connectionParameter;
    }


    public FootPrint getCurrentFieldOfView() {
        if (isApiValid()) {
            try {
                return dpApi.getCurrentFieldOfView();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new FootPrint();
    }


    public Survey buildSurvey(Survey survey) {
        if (isApiValid()) {
            try {
                Survey updated = dpApi.buildSurvey(survey);
                if (updated != null)
                    survey.copy(updated);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return survey;
    }


    public StructureScanner buildStructureScanner(StructureScanner item) {
        if (isApiValid()) {
            try {
                StructureScanner updated = dpApi.buildStructureScanner(item);
                if (updated != null)
                    item.copy(updated);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return item;
    }

    public void registerDroneListener(DroneListener listener){
        if(listener == null)
            return;

        droneListeners.add(listener);
    }

    private void requestEventUpdates(IDroidPlannerApiCallback callback) {
        if(isApiValid()){
            try {
                this.dpApi.requestEventUpdates(callback);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void unregisterDroneListener(DroneListener listener){
        if(listener == null)
            return;

        droneListeners.remove(listener);
    }

    private void removeEventUpdates(IDroidPlannerApiCallback callback) {
        if(isApiValid()){
            try {
                this.dpApi.removeEventUpdates(callback);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void changeVehicleMode(VehicleMode newMode) {
        if (isApiValid()) {
            try {
                dpApi.changeVehicleMode(newMode);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void refreshParameters() {
        if (isApiValid()) {
            try {
                dpApi.refreshParameters();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void writeParameters(Parameters parameters) {
        if (isApiValid()) {
            try {
                dpApi.writeParameters(parameters);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void setMission(Mission mission, boolean pushToDrone) {
        if (isApiValid()) {
            try {
                dpApi.setMission(mission, pushToDrone);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void generateDronie() {
        if (isApiValid()) {
            try {
                dpApi.generateDronie();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void arm(boolean arm) {
        if (isApiValid()) {
            try {
                dpApi.arm(arm);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void startMagnetometerCalibration(double[] startPointsX, double[] startPointsY,
                                             double[] startPointsZ) {
        if (isApiValid()) {
            try {
                dpApi.startMagnetometerCalibration(startPointsX, startPointsY, startPointsZ);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void stopMagnetometerCalibration() {
        if (isApiValid()) {
            try {
                dpApi.stopMagnetometerCalibration();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void startIMUCalibration() {
        if (isApiValid()) {
            try {
                dpApi.startIMUCalibration();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void sendIMUCalibrationAck(int step) {
        if (isApiValid()) {
            try {
                dpApi.sendIMUCalibrationAck(step);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void doGuidedTakeoff(double altitude) {
        if (isApiValid()) {
            try {
                dpApi.doGuidedTakeoff(altitude);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void pauseAtCurrentLocation() {
        sendGuidedPoint(getGps().getPosition(), true);
    }


    public void sendGuidedPoint(LatLong point, boolean force) {
        if (isApiValid()) {
            try {
                dpApi.sendGuidedPoint(point, force);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void setGuidedAltitude(double altitude) {
        if (isApiValid()) {
            try {
                dpApi.setGuidedAltitude(altitude);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void setGuidedVelocity(double xVel, double yVel, double zVel) {
        if (isApiValid()) {
            try {
                dpApi.setGuidedVelocity(xVel, yVel, zVel);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void enableFollowMe(FollowType followType) {
        if (isApiValid()) {
            try {
                dpApi.enableFollowMe(followType);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void setFollowMeRadius(double radius) {
        if (isApiValid()) {
            try {
                dpApi.setFollowMeRadius(radius);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void disableFollowMe() {
        if (isApiValid()) {
            try {
                dpApi.disableFollowMe();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void triggerCamera() {
        if (isApiValid()) {
            try {
                dpApi.triggerCamera();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void epmCommand(boolean release) {
        if (isApiValid()) {
            try {
                dpApi.epmCommand(release);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void loadWaypoints() {
        if (isApiValid()) {
            try {
                dpApi.loadWaypoints();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    void notifyDroneConnectionFailed(final ConnectionResult result) {
        if(droneListeners.isEmpty())
            return;

        handler.post(new Runnable() {
            @Override
            public void run() {
                for(DroneListener listener : droneListeners)
                    listener.onDroneConnectionFailed(result);
            }
        });
    }

    void notifyDroneEvent(final String event, final Bundle extras) {
        if (Event.EVENT_STATE.equals(event)) {
            if (getState().isFlying())
                startTimer();
            else
                stopTimer();
        } else if (Event.EVENT_SPEED.equals(event)) {
            checkForGroundCollision();
        }

        if(droneListeners.isEmpty())
            return;

        handler.post(new Runnable() {
            @Override
            public void run() {
                for(DroneListener listener : droneListeners)
                    listener.onDroneEvent(event, extras);
            }
        });
    }

    void notifyDroneServiceInterrupted(final String errorMsg) {
        if(droneListeners.isEmpty())
            return;

        handler.post(new Runnable() {
            @Override
            public void run() {
                for(DroneListener listener : droneListeners)
                    listener.onDroneServiceInterrupted(errorMsg);
            }
        });
    }
}
