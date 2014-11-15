// IDroidPlannerCallbackApi.aidl
package com.o3dr.services.android.lib.model;

import com.o3dr.services.android.lib.drone.connection.ConnectionResult;

/**
* Callback interface used to send drone property updates back to the clients.
* Note that this is a one way interface so the server doesn't block waiting for the client.
*/
oneway interface IDroidPlannerApiCallback {

    /**
    * Called when the connection attempt fails.
    * @param result Describe why the connection failed.
    */
    void onConnectionFailed(in ConnectionResult result);

    /**
    * Called when a drone property is updated.
    * @param droneId id of the drone whose property was updated.
    * @param event string representing the event.
    */
    void onDroneEvent(String event, in Bundle eventExtras);

}
