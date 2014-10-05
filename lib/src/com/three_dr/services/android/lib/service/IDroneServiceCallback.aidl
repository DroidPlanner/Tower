// IDroneApiCallback.aidl
package com.three_dr.services.android.lib.service;

// Declare any non-default types here with import statements


/**
* Callback interface used by IDroneService to send drone updates notifications back to the clients.
* Note that this is a one way interface so the server doesn't block waiting for the client.
*/
oneway interface IDroneServiceCallback {

    /**
    * Called when the drone instance was successfully connected.
    * @param drone drone instance connected to.
    */
    void onConnected(Drone drone);

    /**
    * Called when the connection attempt fails.
    * @param result Describe why the connection failed.
    */
    void onConnectionFailed(ConnectionResult result);

    /**
    * Called when this callback has been unregistered
    */
    void onDisconnect();

    /**
    * Called when the drone instance was updated.
    * @param drone Updated drone instance.
    */
    void onDroneUpdated(Drone drone);
}
