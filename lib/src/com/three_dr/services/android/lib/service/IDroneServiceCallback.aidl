// IDroneServiceCallback.aidl
package com.three_dr.services.android.lib.service;

import com.three_dr.services.android.lib.drone.connection.ConnectionResult;
import com.three_dr.services.android.lib.drone.Drone;

/**
* Callback interface used by IDroneService to send drone updates notifications back to the clients.
* Note that this is a one way interface so the server doesn't block waiting for the client.
*/
oneway interface IDroneServiceCallback {

    /**
    * Called when the drone instance was successfully connected.
    * @param drone drone instance connected to.
    */
    void onConnected(String droneId);

    /**
    * Called when the connection attempt fails.
    * @param result Describe why the connection failed.
    */
    void onConnectionFailed(in ConnectionResult result);

    /**
    * Called when this callback has been unregistered
    */
    void onDisconnect(String droneId);

    /**
    * Called when the drone instance was updated.
    * @param drone Updated drone instance.
    */
    void onDroneUpdated(in Drone drone);
}
