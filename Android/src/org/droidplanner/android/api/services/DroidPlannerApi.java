package org.droidplanner.android.api.services;

import android.content.Intent;
import android.os.Binder;

import org.droidplanner.android.activities.helpers.BluetoothDevicesActivity;
import org.droidplanner.android.communication.service.MAVLinkClient;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.WaypointManager;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.model.Drone;

import java.lang.ref.WeakReference;

/**
* Created by fhuya on 10/24/14.
*/
public class DroidPlannerApi extends Binder {

    private final WeakReference<DroidPlannerService> serviceRef;

    DroidPlannerApi(DroidPlannerService service){
        this.serviceRef = new WeakReference<DroidPlannerService>(service);
    }

    private DroidPlannerService getService(){
        final DroidPlannerService service = this.serviceRef.get();
        if(service == null)
            throw new IllegalStateException("Lost reference to the parent service.");

        return service;
    }

    public void addDroneListener(DroneInterfaces.OnDroneListener listener){
        getDrone().addDroneListener(listener);
    }

    public void notifyDroneEvent(DroneInterfaces.DroneEventsType event){
        getDrone().notifyDroneEvent(event);
    }

    public void removeDroneListener(DroneInterfaces.OnDroneListener listener){
        getDrone().removeDroneListener(listener);
    }

    public GPS getGps(){
        return getDrone().getGps();
    }

    public GuidedPoint getGuidedPoint(){
        return getDrone().getGuidedPoint();
    }

    public org.droidplanner.core.drone.variables.State getState(){
        return getDrone().getState();
    }

    public Mission getMission(){
        return getDrone().getMission();
    }

    public boolean isConnected(){
        return getDrone().getMavClient().isConnected();
    }

    public MAVLinkStreams.MAVLinkOutputStream getMavClient(){
        return getDrone().getMavClient();
    }

    public Drone getDrone(){
        return getService().drone;
    }

    public Follow getFollowMe(){
        return getService().followMe;
    }

    public MissionProxy getMissionProxy(){
        return getService().missionProxy;
    }

    void toggleDroneConnection(){
        final DroidPlannerService service = getService();
        final Drone drone = service.drone;
        final DroidPlannerPrefs prefs = service.appPrefs;

        if (!drone.getMavClient().isConnected()) {
            final String connectionType = prefs.getMavLinkConnectionType();

            if (Utils.ConnectionType.BLUETOOTH.name().equals(connectionType)) {
                // Launch a bluetooth device selection screen for the user
                final String address = prefs.getBluetoothDeviceAddress();
                if (address == null || address.isEmpty()) {
                    service.startActivity(
                            new Intent(service.getApplicationContext(),
                                    BluetoothDevicesActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    return;
                }
            }
        }
        drone.getMavClient().toggleConnectionState();
    }

    public WaypointManager getWaypointManager() {
        return getDrone().getWaypointManager();
    }
}
