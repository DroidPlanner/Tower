package com.o3dr.android.client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;

import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.model.IDroidPlannerApiCallback;

/**
 * Created by fhuya on 10/29/14.
 */
public final class DroneCallback extends IDroidPlannerApiCallback.Stub {

    private final Drone drone;

	public DroneCallback(Drone drone) {
        this.drone = drone;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) throws RemoteException {
        drone.notifyDroneConnectionFailed(result);
	}

	@Override
	public void onDroneEvent(String event, Bundle eventExtras) throws RemoteException {
        drone.notifyDroneEvent(event, eventExtras);
	}
}
