// IDroneApi.aidl
package com.three_dr.services.android.lib.service;

// Declare any non-default types here with import statements
import com.three_dr.services.android.lib.service.IDroneServiceCallback;

/**
* Interface used to access the drone api from another (client) process.
*/
interface IDroneService {
    /**
    * Blocking call used to establish connection with a drone, or retrieve an existing drone
    * instance whose connection parameters match the passed one.
    * The 'clientCb' argument must be non-null, and allows the called to receive updates to the
    * returned Drone instance.
    *
    * @param params ConnectionParameter object
    * @param clientCb Client callback instance
    * @return Drone instance
    */
    Drone connect(ConnectionParameter params, IDroneServiceCallback clientCb);

    /**
    * Asynchronous call used to stop listening to updates for the drone instance whose id is
    * specified by 'droneId'.
    * If no other clients is listening to the drone instance, connection with the underlying
    * vehicle will be broken.
    *
    * @param droneId Id of the drone instance to disconnect from
    * @param clientCb Non-null callback. The specified callback will be unregistered.
    */
    oneway void disconnect(String droneId, IDroneServiceCallback clientCb);

    /**
    * Allows a client to grab an updated copy of the drone specified by the passed argument.
    * @param droneId Id of the drone instance to refresh.
    */
    Drone refreshDrone(String droneId);

    /**
    * Send a request for an update to be applied to the drone identified by the passed 'droneId'.
    *
    * @param droneId Id of the drone to apply the update to.
    * @param updateRequest update to be performed on the specified drone.
    */
    oneway void updateDrone(String droneId, DroneUpdateRequest updateRequest);


}
