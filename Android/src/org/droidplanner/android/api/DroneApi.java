package org.droidplanner.android.api;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ox3dr.services.android.lib.drone.property.Altitude;
import com.ox3dr.services.android.lib.drone.property.Attitude;
import com.ox3dr.services.android.lib.drone.property.Battery;
import com.ox3dr.services.android.lib.drone.property.Gps;
import com.ox3dr.services.android.lib.drone.property.Home;
import com.ox3dr.services.android.lib.drone.property.Mission;
import com.ox3dr.services.android.lib.drone.property.Parameters;
import com.ox3dr.services.android.lib.drone.property.Speed;
import com.ox3dr.services.android.lib.drone.property.State;
import com.ox3dr.services.android.lib.drone.property.Type;
import com.ox3dr.services.android.lib.drone.property.VehicleMode;
import com.ox3dr.services.android.lib.model.IDroidPlannerApi;

import org.droidplanner.android.DroidPlannerApp;

/**
 * Created by fhuya on 11/4/14.
 */
public class DroneApi implements com.ox3dr.services.android.lib.model.IDroidPlannerApi {

    private static final String TAG = DroneApi.class.getSimpleName();

    private final LocalBroadcastManager lbm;
    private IDroidPlannerApi dpApi;

    public DroneApi(Context context, IDroidPlannerApi dpApi){
        this.dpApi = dpApi;
        lbm = LocalBroadcastManager.getInstance(context);
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
    public IBinder asBinder() {
        throw new UnsupportedOperationException();
    }
}
