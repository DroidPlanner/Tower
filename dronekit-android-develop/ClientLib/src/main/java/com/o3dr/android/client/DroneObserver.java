package com.o3dr.android.client;

import android.os.Bundle;
import android.os.RemoteException;

import com.o3dr.services.android.lib.model.IObserver;

/**
 * Created by fhuya on 10/29/14.
 */
final class DroneObserver extends IObserver.Stub {

    private final Drone drone;

    public DroneObserver(Drone drone) {
        this.drone = drone;
    }

    @Override
    public void onAttributeUpdated(String attributeEvent, Bundle eventExtras) throws
            RemoteException {
        drone.notifyAttributeUpdated(attributeEvent, eventExtras);
    }
}
