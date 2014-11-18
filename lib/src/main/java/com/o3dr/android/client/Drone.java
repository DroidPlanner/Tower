package com.o3dr.android.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fhuya on 11/4/14.
 */
public class Drone implements com.o3dr.services.android.lib.model.IDroidPlannerApi,
        ServiceListener {

    private static final String CLAZZ_NAME = Drone.class.getName();
    private static final String TAG = Drone.class.getSimpleName();

    public static final int COLLISION_SECONDS_BEFORE_COLLISION = 2;
    public static final double COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND = -3.0;
    public static final double COLLISION_SAFE_ALTITUDE_METERS = 1.0;

    public static final String ACTION_GROUND_COLLISION_IMMINENT = CLAZZ_NAME +
            ".ACTION_GROUND_COLLISION_IMMINENT";
    public static final String EXTRA_IS_GROUND_COLLISION_IMMINENT =
            "extra_is_ground_collision_imminent";

    private final static IntentFilter intentFilter = new IntentFilter();

    static {
        intentFilter.addAction(Event.EVENT_STATE);
        intentFilter.addAction(Event.EVENT_SPEED);
        intentFilter.addAction(DroneCallback.ACTION_DRONE_CONNECTION_FAILED);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Event.EVENT_STATE.equals(action)) {
                if (getState().isFlying())
                    startTimer();
                else
                    stopTimer();
            } else if (Event.EVENT_SPEED.equals(action)) {
                checkForGroundCollision();
            } else if (DroneCallback.ACTION_DRONE_CONNECTION_FAILED.equals(action)) {
                disconnect();
            }
        }
    };


    private final ServiceManager serviceMgr;
    private final LocalBroadcastManager lbm;
    private final DroneCallback droneCallback;
    private IDroidPlannerApi dpApi;

    private Runnable onConnectedTask;
    private ConnectionParameter connectionParameter;

    // flightTimer
    // ----------------
    private long startTime = 0;
    private long elapsedFlightTime = 0;
    private AtomicBoolean isTimerRunning = new AtomicBoolean(false);

    public Drone(Context context, ServiceManager serviceManager) {
        this.serviceMgr = serviceManager;
        this.droneCallback = new DroneCallback(context);
        lbm = LocalBroadcastManager.getInstance(context);

        this.serviceMgr.addServiceListener(this);
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

        lbm.sendBroadcast(new Intent(ACTION_GROUND_COLLISION_IMMINENT)
                .putExtra(EXTRA_IS_GROUND_COLLISION_IMMINENT, isCollisionImminent));
    }

    private void start() throws RemoteException {
        this.dpApi = serviceMgr.get3drServices().acquireDroidPlannerApi(this.droneCallback);

        resetFlightTimer();
        lbm.registerReceiver(broadcastReceiver, intentFilter);
    }

    private void terminate() {
        lbm.unregisterReceiver(broadcastReceiver);
        this.dpApi = null;
        try {
            serviceMgr.get3drServices().releaseDroidPlannerApi(this.droneCallback);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void handleRemoteException(RemoteException e) {
        serviceMgr.handleRemoteException(e);
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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
        else{
            onConnectedTask = new Runnable() {
                @Override
                public void run() {
                    try {
                        dpApi.connect(connParams);
                        connectionParameter = connParams;
                    } catch (RemoteException e) {
                        handleRemoteException(e);
                    }
                }
            };
        }
    }

    public void disconnect() {
        onConnectedTask = null;

        if (isApiValid()) {
            try {
                dpApi.disconnect();
                this.connectionParameter = null;
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    private boolean isApiValid(){
        return dpApi != null;
    }

    @Override
    public boolean isConnected() {
        try {
            return isApiValid() && dpApi.isConnected();
        } catch (RemoteException e) {
            handleRemoteException(e);
        }
        return false;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    public ConnectionParameter getConnectionParameter(){
        return this.connectionParameter;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void changeVehicleMode(VehicleMode newMode) {
        if (isApiValid()) {
            try {
                dpApi.changeVehicleMode(newMode);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void refreshParameters() {
        if (isApiValid()) {
            try {
                dpApi.refreshParameters();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void writeParameters(Parameters parameters) {
        if (isApiValid()) {
            try {
                dpApi.writeParameters(parameters);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void setMission(Mission mission, boolean pushToDrone) {
        if (isApiValid()) {
            try {
                dpApi.setMission(mission, pushToDrone);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void generateDronie() {
        if (isApiValid()) {
            try {
                dpApi.generateDronie();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void arm(boolean arm) {
        if (isApiValid()) {
            try {
                dpApi.arm(arm);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
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

    @Override
    public void stopMagnetometerCalibration() {
        if (isApiValid()) {
            try {
                dpApi.stopMagnetometerCalibration();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void startIMUCalibration() {
        if (isApiValid()) {
            try {
                dpApi.startIMUCalibration();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void sendIMUCalibrationAck(int step) {
        if (isApiValid()) {
            try {
                dpApi.sendIMUCalibrationAck(step);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
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

    @Override
    public void sendGuidedPoint(LatLong point, boolean force) {
        if (isApiValid()) {
            try {
                dpApi.sendGuidedPoint(point, force);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void setGuidedAltitude(double altitude) {
        if (isApiValid()) {
            try {
                dpApi.setGuidedAltitude(altitude);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void setGuidedVelocity(double xVel, double yVel, double zVel) {
        if (isApiValid()) {
            try {
                dpApi.setGuidedVelocity(xVel, yVel, zVel);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void enableFollowMe(FollowType followType) {
        if (isApiValid()) {
            try {
                dpApi.enableFollowMe(followType);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void setFollowMeRadius(double radius) {
        if (isApiValid()) {
            try {
                dpApi.setFollowMeRadius(radius);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void disableFollowMe() {
        if (isApiValid()) {
            try {
                dpApi.disableFollowMe();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void triggerCamera() {
        if (isApiValid()) {
            try {
                dpApi.triggerCamera();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void epmCommand(boolean release) {
        if (isApiValid()) {
            try {
                dpApi.epmCommand(release);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void loadWaypoints() {
        if (isApiValid()) {
            try {
                dpApi.loadWaypoints();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public IBinder asBinder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onServiceConnected() {
        try {
            start();
            if(onConnectedTask != null) {
                onConnectedTask.run();
                onConnectedTask = null;
            }
        } catch (RemoteException e) {
            handleRemoteException(e);
        }
    }

    @Override
    public void onServiceDisconnected() {
        try {
            terminate();
        } catch (RemoteException e) {
            handleRemoteException(e);
        }
    }
}
