package org.droidplanner.android.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ox3dr.services.android.lib.coordinate.LatLongAlt;
import com.ox3dr.services.android.lib.coordinate.Point3D;
import com.ox3dr.services.android.lib.drone.event.Event;
import com.ox3dr.services.android.lib.drone.property.Altitude;
import com.ox3dr.services.android.lib.drone.property.Attitude;
import com.ox3dr.services.android.lib.drone.property.Battery;
import com.ox3dr.services.android.lib.drone.property.Gps;
import com.ox3dr.services.android.lib.drone.property.GuidedState;
import com.ox3dr.services.android.lib.drone.property.Home;
import com.ox3dr.services.android.lib.drone.property.Mission;
import com.ox3dr.services.android.lib.drone.property.Parameters;
import com.ox3dr.services.android.lib.drone.property.Signal;
import com.ox3dr.services.android.lib.drone.property.Speed;
import com.ox3dr.services.android.lib.drone.property.State;
import com.ox3dr.services.android.lib.drone.property.Type;
import com.ox3dr.services.android.lib.drone.property.VehicleMode;
import com.ox3dr.services.android.lib.model.IDroidPlannerApi;

import org.droidplanner.android.DroidPlannerApp;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fhuya on 11/4/14.
 */
public class DroneApi implements com.ox3dr.services.android.lib.model.IDroidPlannerApi {

    private static final String TAG = DroneApi.class.getSimpleName();

    private final static IntentFilter intentFilter = new IntentFilter(Event.EVENT_STATE);

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
        }
    };

    private final LocalBroadcastManager lbm;
    private IDroidPlannerApi dpApi;

    // flightTimer
    // ----------------
    private long startTime = 0;
    private long elapsedFlightTime = 0;
    private AtomicBoolean isTimerRunning = new AtomicBoolean(false);

    public DroneApi(Context context, IDroidPlannerApi dpApi){
        this.dpApi = dpApi;
        lbm = LocalBroadcastManager.getInstance(context);
        lbm.registerReceiver(broadcastReceiver, intentFilter);
    }

    private void handleRemoteException(RemoteException e){
        Log.e(TAG, e.getMessage(), e);
    }

    private boolean isApiValid(){
        return dpApi != null;
    }

    public void setDpApi(IDroidPlannerApi dpApi) {
        this.dpApi = dpApi;
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
        if (getState().isFlying()) {
            // calc delta time since last checked
            elapsedFlightTime += SystemClock.elapsedRealtime() - startTime;
            startTime = SystemClock.elapsedRealtime();
        }
        return elapsedFlightTime / 1000;
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

    public void connect(){
        lbm.sendBroadcast(new Intent(DroidPlannerApp.ACTION_TOGGLE_DRONE_CONNECTION)
        .putExtra(DroidPlannerApp.EXTRA_ESTABLISH_CONNECTION, true));
    }

    public void disconnect(){
        lbm.sendBroadcast(new Intent(DroidPlannerApp.ACTION_TOGGLE_DRONE_CONNECTION)
                .putExtra(DroidPlannerApp.EXTRA_ESTABLISH_CONNECTION, false));
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
    public void disconnectFromDrone()  {
        if(isApiValid()){
            try {
                dpApi.disconnectFromDrone();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }

        lbm.unregisterReceiver(broadcastReceiver);
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
    public void sendMission(Mission mission)  {
        if(isApiValid()){
            try {
                dpApi.sendMission(mission);
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

    @Override
    public void sendGuidedPoint(LatLongAlt point, boolean force) {
        if(isApiValid()){
            try {
                dpApi.sendGuidedPoint(point, force);
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
