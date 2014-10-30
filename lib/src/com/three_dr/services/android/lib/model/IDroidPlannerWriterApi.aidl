// IDroidPlannerWriterApi.aidl
package com.three_dr.services.android.lib.model;

import com.three_dr.services.android.lib.drone.connection.ConnectionParameter;
import com.three_dr.services.android.lib.model.IDroidPlannerCallbackApi;
import com.three_dr.services.android.lib.drone.property.Parameters;
import com.three_dr.services.android.lib.drone.property.Mission;

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
    * @param newMode enum name of the new vehicle mode.
    */
    void changeVehicleMode(int droneId, String newModeName);

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

    /**
    * Refresh the parameters for the specified drone.
    * @param droneId id of the drone whose parameters to refresh.
    */
    void refreshParameters(int droneId);

    /**
    * Write the given parameters to the specified drone.
    * @param droneId id of the drone whose parameters to write.
    */
    void writeParameters(int droneId, in Parameters parameters);

    /**
    * Upload the given mission to the specified drone.
    * @param droneId id of the drone to which the mission is uploaded.
    * @param mission mission to upload to the drone.
    */
    void sendMission(int droneId, in Mission mission);
}
