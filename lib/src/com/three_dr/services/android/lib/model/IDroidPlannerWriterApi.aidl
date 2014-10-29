// IDroidPlannerWriterApi.aidl
package com.three_dr.services.android.lib.model;

import com.three_dr.services.android.lib.drone.connection.ConnectionParameter;
import com.three_dr.services.android.lib.model.IDroidPlannerCallbackApi;

/**
* Interface used to establish/break connection with a drone.
*/
oneway interface IDroidPlannerWriterApi {

    /**
    * Asynchronous call used to establish connection with a drone, or retrieve an existing drone
    * instance whose connection parameters match the passed one.
    * The 'callback' argument must be non-null, and allows to receive updates about the connected
    * drone instance.
    *
    * @param params ConnectionParameter object
    * @param callback Client callback instance
    */
    void connectToDrone(in ConnectionParameter params, IDroidPlannerCallbackApi callback);

    /**
    * Change the vehicle mode for the specified drone.
    * @param droneId id of the drone instance whose mode to update.
    * @param newMode new vehicle mode.
    */
    void changeVehicleMode(int droneId, VehicleMode newMode);

    /**
    * Asynchronous call used to stop listening to updates for the drone instance whose id is
    * specified by 'droneId'.
    * If no other clients is listening to the drone instance, connection with the underlying
    * vehicle will be broken.
    *
    * @param droneId Id of the drone instance to disconnect from
    * @param clientCb Non-null callback. The specified callback will be unregistered.
    */
    void disconnectFromDrone(int droneId, IDroidPlannerCallbackApi callback);
}
