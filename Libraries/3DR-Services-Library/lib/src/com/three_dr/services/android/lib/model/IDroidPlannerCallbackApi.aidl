// IDroidPlannerCallbackApi.aidl
package com.three_dr.services.android.lib.model;

import com.three_dr.services.android.lib.drone.connection.ConnectionResult;

/**
* Callback interface used to send drone property updates back to the clients.
* Note that this is a one way interface so the server doesn't block waiting for the client.
*/
oneway interface IDroidPlannerCallbackApi {

    /**
    * Called when the drone instance was successfully connected.
    * @param droneId id of the drone instance connected to.
    */
    void onConnected(int droneId);

    /**
    * Called when the connection attempt fails.
    * @param result Describe why the connection failed.
    */
    void onConnectionFailed(in ConnectionResult result);

    /**
    * Called when this callback has been unregistered
    * @param droneId id of the drone instance disconnected from.
    */
    void onDisconnect(int droneId);

    /**
    * Called when a drone property is updated.
    * @param droneId id of the drone whose property was updated.
    * @param propertyType type of the updated property.
    */
    void onPropertyChanged(int droneId, String propertyType);
}
