package org.droidplanner.android.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ox3dr.services.android.lib.coordinate.LatLong;
import com.ox3dr.services.android.lib.coordinate.Point3D;
import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.ox3dr.services.android.lib.drone.connection.ConnectionType;
import com.ox3dr.services.android.lib.drone.event.Event;
import com.ox3dr.services.android.lib.drone.mission.Mission;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItem;
import com.ox3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.ox3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.ox3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.ox3dr.services.android.lib.drone.mission.item.raw.MissionItemMessage;
import com.ox3dr.services.android.lib.drone.property.Altitude;
import com.ox3dr.services.android.lib.drone.property.Attitude;
import com.ox3dr.services.android.lib.drone.property.Battery;
import com.ox3dr.services.android.lib.drone.property.FootPrint;
import com.ox3dr.services.android.lib.drone.property.Gps;
import com.ox3dr.services.android.lib.drone.property.GuidedState;
import com.ox3dr.services.android.lib.drone.property.Home;
import com.ox3dr.services.android.lib.drone.property.Parameter;
import com.ox3dr.services.android.lib.drone.property.Parameters;
import com.ox3dr.services.android.lib.drone.property.Signal;
import com.ox3dr.services.android.lib.drone.property.Speed;
import com.ox3dr.services.android.lib.drone.property.State;
import com.ox3dr.services.android.lib.drone.property.Type;
import com.ox3dr.services.android.lib.drone.property.VehicleMode;
import com.ox3dr.services.android.lib.gcs.follow.FollowState;
import com.ox3dr.services.android.lib.gcs.follow.FollowType;
import com.ox3dr.services.android.lib.model.IDroidPlannerApi;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.helpers.BluetoothDevicesActivity;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fhuya on 11/4/14.
 */
public class Drone implements com.ox3dr.services.android.lib.model.IDroidPlannerApi {

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
        intentFilter.addAction(Event.EVENT_MISSION_DRONIE_CREATED);
        intentFilter.addAction(Event.EVENT_MISSION_UPDATE);
        intentFilter.addAction(Event.EVENT_MISSION_RECEIVED);
        intentFilter.addAction(Event.EVENT_SPEED);
        intentFilter.addAction(DPApiCallback.ACTION_DRONE_CONNECTION_FAILED);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(Event.EVENT_STATE.equals(action)){
                if(getState().isFlying())
                    startTimer();
                else
                    stopTimer();
            }
            else if(Event.EVENT_MISSION_DRONIE_CREATED.equals(action)
                    || Event.EVENT_MISSION_UPDATE.equals(action)
                    || Event.EVENT_MISSION_RECEIVED.equals(action)){
                missionProxy.load(getMission());
            }
            else if(Event.EVENT_SPEED.equals(action)){
                checkForGroundCollision();
            }
            else if(DPApiCallback.ACTION_DRONE_CONNECTION_FAILED.equals(action)){
                disconnect();
            }
        }
    };

    private void checkForGroundCollision() {
        Speed speed = getSpeed();
        Altitude altitude = getAltitude();
        if(speed == null || altitude == null)
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

    private final LocalBroadcastManager lbm;
    private final MissionProxy missionProxy;
    private IDroidPlannerApi dpApi;

    // flightTimer
    // ----------------
    private long startTime = 0;
    private long elapsedFlightTime = 0;
    private AtomicBoolean isTimerRunning = new AtomicBoolean(false);

    private final DroidPlannerPrefs dpPrefs;
    private final DroidPlannerApp dpApp;
    private final DPApiCallback dpCallback;
    
    private final LinkedList<Runnable> onConnectedTasks = new LinkedList<Runnable>(); 

    public Drone(DroidPlannerApp dpApp){
        this.dpApp = dpApp;
        dpCallback = new DPApiCallback(dpApp);

        final Context context = dpApp.getApplicationContext();
        dpPrefs = new DroidPlannerPrefs(context);
        this.missionProxy = new MissionProxy(context, this);
        lbm = LocalBroadcastManager.getInstance(context);
    }

    public void start(){
        resetFlightTimer();
        registerWithDrone();
        lbm.registerReceiver(broadcastReceiver, intentFilter);

        if(onConnectedTasks.isEmpty())
            return;

        for(Runnable tasks: onConnectedTasks)
            tasks.run();
    }

    public void terminate(){
        disconnect();
        lbm.unregisterReceiver(broadcastReceiver);
        unregisterFromDrone();
    }

    private void handleRemoteException(RemoteException e){
        Log.e(TAG, e.getMessage(), e);
        registerWithDrone();
    }

    private ConnectionParameter retrieveConnectionParameters() {
        final int connectionType = dpPrefs.getConnectionParameterType();
        Bundle extraParams = new Bundle();

        ConnectionParameter connParams;
        switch (connectionType) {
            case ConnectionType.TYPE_USB:
                extraParams.putInt(ConnectionType.EXTRA_USB_BAUD_RATE, dpPrefs.getUsbBaudRate());
                connParams = new ConnectionParameter(connectionType, extraParams);
                break;

            case ConnectionType.TYPE_UDP:
                extraParams.putInt(ConnectionType.EXTRA_UDP_SERVER_PORT, dpPrefs.getUdpServerPort());
                connParams = new ConnectionParameter(connectionType, extraParams);
                break;

            case ConnectionType.TYPE_TCP:
                extraParams.putString(ConnectionType.EXTRA_TCP_SERVER_IP, dpPrefs.getTcpServerIp());
                extraParams.putInt(ConnectionType.EXTRA_TCP_SERVER_PORT, dpPrefs.getTcpServerPort());
                connParams = new ConnectionParameter(connectionType, extraParams);
                break;

            case ConnectionType.TYPE_BLUETOOTH:
                String btAddress = dpPrefs.getBluetoothDeviceAddress();
                if (TextUtils.isEmpty(btAddress)) {
                    connParams = null;
                    dpApp.startActivity(new Intent(dpApp.getApplicationContext(),
                            BluetoothDevicesActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                } else {
                    extraParams.putString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS, btAddress);
                    connParams = new ConnectionParameter(connectionType, extraParams);
                }
                break;

            default:
                Log.e(TAG, "Unrecognized connection type: " + connectionType);
                connParams = null;
                break;
        }

        return connParams;
    }

    public double getSpeedParameter(){
        Parameters params = getParameters();
        if(params != null) {
            Parameter speedParam = params.getParameter("WPNAV_SPEED");
            if(speedParam != null)
                return speedParam.getValue();
        }

        return 0;
    }

    private boolean isApiValid(){
        return registerWithDrone();
    }

    private boolean isApiValid(Runnable toDoWhenApiIsValid){
        boolean isValid = registerWithDrone();
        if(!isValid && toDoWhenApiIsValid != null)
            onConnectedTasks.add(toDoWhenApiIsValid);

        return isValid;
    }

    public void resetFlightTimer() {
        elapsedFlightTime = 0;
        startTime = SystemClock.elapsedRealtime();
        isTimerRunning.set(true);
    }

    public void startTimer() {
        if(isTimerRunning.compareAndSet(false, true))
            startTime = SystemClock.elapsedRealtime();
    }

    public void stopTimer() {
        if(isTimerRunning.compareAndSet(true, false)) {
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

    public MissionProxy getMissionProxy(){
        return this.missionProxy;
    }

    @Override
    public Gps getGps(){
        if(isApiValid()){
            try {
                return dpApi.getGps();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }

        return null;
    }

    @Override
    public State getState(){
        if(isApiValid()){
            try {
                return dpApi.getState();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }

        return null;
    }

    @Override
    public VehicleMode[] getAllVehicleModes()  {
        if(isApiValid()){
            try {
                return dpApi.getAllVehicleModes();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new VehicleMode[0];
    }

    @Override
    public Parameters getParameters()  {
        if(isApiValid()){
            try {
                return dpApi.getParameters();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return null;
    }

    @Override
    public Speed getSpeed()  {
        if(isApiValid()){
            try {
                return dpApi.getSpeed();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return null;
    }

    @Override
    public Attitude getAttitude()  {
        if(isApiValid()){
            try {
                return dpApi.getAttitude();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return null;
    }

    @Override
    public Home getHome()  {
        if(isApiValid()){
            try {
                return dpApi.getHome();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return null;
    }

    @Override
    public Battery getBattery()  {
        if(isApiValid()){
            try {
                return dpApi.getBattery();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return null;
    }

    @Override
    public Altitude getAltitude()  {
        if(isApiValid()){
            try {
                return dpApi.getAltitude();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return null;
    }

    @Override
    public Mission getMission()  {
        if(isApiValid()){
            try {
                return dpApi.getMission();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return null;
    }

    @Override
    public MissionItemMessage[] processMissionItems(MissionItem[] missionItems) {
        if(isApiValid()){
            try {
                return dpApi.processMissionItems(missionItems);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new MissionItemMessage[0];
    }

    @Override
    public Signal getSignal() {
        if(isApiValid()){
            try {
                return dpApi.getSignal();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }

        return null;
    }

    @Override
    public Type getType()  {
        if(isApiValid()){
            try {
                return dpApi.getType();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return null;
    }

    private boolean registerWithDrone(){
        if(!dpApp.is3drServicesConnected()) {
            dpApp.connect();
            return false;
        }

        if (dpApi != null)
            return true;

        // Retrieve the connection parameters.
        final ConnectionParameter connParams = retrieveConnectionParameters();
        if (connParams == null) {
            throw new IllegalArgumentException("Invalid connection parameters.");
        }

        try {
            dpApi = dpApp.get3drServices().registerWithDrone(connParams, dpCallback);
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to retrieve a droidplanner api connection.", e);
            dpApp.connect();
            return false;
        }        
        
        return true;
    }

    private void unregisterFromDrone(){
        dpApi = null;

        if (!dpApp.is3drServicesConnected())
            return; // Nothing to do. It's already disconnected.

        try {
            dpApp.get3drServices().unregisterFromDrone(retrieveConnectionParameters(), dpCallback);
        } catch (RemoteException e) {
            Log.e(TAG, "Error while disconnecting from the droidplanner api", e);
        }
    }

    private final Runnable connectTask = new Runnable() {
        @Override
        public void run() {
            connect();
        }
    };

	public void connect() {
        if(isApiValid(connectTask))
		try {
			dpApi.connect();
            missionProxy.load(getMission());
            lbm.sendBroadcast(new Intent(Event.EVENT_CONNECTED));
		} catch (RemoteException e) {
			handleRemoteException(e);
		}
	}

    public void disconnect(){
        if(isApiValid()){
            try {
                dpApi.disconnect();
                lbm.sendBroadcast(new Intent(Event.EVENT_DISCONNECTED));
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public boolean isConnected()  {
        if(isApiValid()){
            try {
                return dpApi.isConnected();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return false;
    }

    @Override
    public GuidedState getGuidedState(){
        if(isApiValid()){
            try {
                return dpApi.getGuidedState();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return null;
    }

    @Override
    public FollowState getFollowState() {
        if(isApiValid()){
            try {
                return dpApi.getFollowState();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return null;
    }

    @Override
    public FollowType[] getFollowTypes() {
        if(isApiValid()){
            try {
                return dpApi.getFollowTypes();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new FollowType[0];
    }

    @Override
    public CameraDetail[] getCameraDetails()  {
        if(isApiValid()){
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
        if(isApiValid()){
            try {
                return dpApi.getLastCameraFootPrint();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return null;
    }

    @Override
    public FootPrint[] getCameraFootPrints() {
        if(isApiValid()){
            try {
                return dpApi.getCameraFootPrints();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return new FootPrint[0];
    }

    @Override
    public Survey buildSurvey(Survey survey) {
        if(isApiValid()){
            try {
                Survey updated = dpApi.buildSurvey(survey);
                if(updated != null)
                    survey.copy(updated);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return null;
    }

    @Override
    public StructureScanner buildStructureScanner(StructureScanner item) {
        if(isApiValid()){
            try {
                StructureScanner updated = dpApi.buildStructureScanner(item);
                if(updated != null)
                    item.copy(updated);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return null;
    }

    @Override
    public void changeVehicleMode(VehicleMode newMode)  {
        if(isApiValid()){
            try {
                dpApi.changeVehicleMode(newMode);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void refreshParameters()  {
        if(isApiValid()){
            try {
                dpApi.refreshParameters();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void writeParameters(Parameters parameters)  {
        if(isApiValid()){
            try {
                dpApi.writeParameters(parameters);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void setMission(Mission mission, boolean pushToDrone)  {
        if(isApiValid()){
            try {
                dpApi.setMission(mission, pushToDrone);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void setRawMissionItems(MissionItemMessage[] missionItems, boolean pushToDrone) {
        if(isApiValid()){
            try {
                dpApi.setRawMissionItems(missionItems, pushToDrone);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void generateDronie() {
        if(isApiValid()){
            try {
                dpApi.generateDronie();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void arm(boolean arm)  {
        if(isApiValid()){
            try {
                dpApi.arm(arm);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void startMagnetometerCalibration(List<Point3D> startPoints) {
        if(isApiValid()){
            try {
                dpApi.startMagnetometerCalibration(startPoints);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void stopMagnetometerCalibration() {
        if(isApiValid()){
            try {
                dpApi.stopMagnetometerCalibration();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void startIMUCalibration() {
        if(isApiValid()){
            try {
                dpApi.startIMUCalibration();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void sendIMUCalibrationAck(int step)  {
        if(isApiValid()){
            try {
                dpApi.sendIMUCalibrationAck(step);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void doGuidedTakeoff(double altitude) {
        if(isApiValid()){
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
        if(isApiValid()){
            try {
                dpApi.sendGuidedPoint(point, force);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void setGuidedAltitude(double altitude){
        if(isApiValid()){
            try {
                dpApi.setGuidedAltitude(altitude);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void setGuidedVelocity(double xVel, double yVel, double zVel)  {
        if(isApiValid()){
            try {
                dpApi.setGuidedVelocity(xVel, yVel, zVel);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void enableFollowMe(FollowType followType)  {
        if(isApiValid()){
            try {
                dpApi.enableFollowMe(followType);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void setFollowMeRadius(double radius){
        if(isApiValid()){
            try {
                dpApi.setFollowMeRadius(radius);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void disableFollowMe() {
        if(isApiValid()){
            try {
                dpApi.disableFollowMe();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void enableDroneShare(String username, String password, boolean isEnabled) {
        if(isApiValid()){
            try {
                dpApi.enableDroneShare(username, password, isEnabled);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void triggerCamera()  {
        if(isApiValid()){
            try {
                dpApi.triggerCamera();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void epmCommand(boolean release) {
        if(isApiValid()){
            try {
                dpApi.epmCommand(release);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    @Override
    public void loadWaypoints() {
        if(isApiValid()){
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
}
