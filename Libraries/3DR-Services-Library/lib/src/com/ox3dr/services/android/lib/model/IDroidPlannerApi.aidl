// IDroidPlannerApi.aidl
package com.ox3dr.services.android.lib.model;

import com.ox3dr.services.android.lib.drone.property.Gps;
import com.ox3dr.services.android.lib.drone.property.Home;
import com.ox3dr.services.android.lib.drone.property.Speed;
import com.ox3dr.services.android.lib.drone.property.Attitude;
import com.ox3dr.services.android.lib.drone.property.Altitude;
import com.ox3dr.services.android.lib.drone.property.Battery;
import com.ox3dr.services.android.lib.drone.property.Parameters;
import com.ox3dr.services.android.lib.drone.property.Mission;
import com.ox3dr.services.android.lib.drone.property.State;
import com.ox3dr.services.android.lib.drone.property.VehicleMode;

/**
* Interface used to access the drone properties.
*/
interface IDroidPlannerApi {
    /**
        * Retrieves the gps property of the connected drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Gps getGps();

        /**
        * Retrieves the state property of the connected drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        State getState();

        /**
        * Retrieves all the vehicle modes for the connected drone.
        */
        VehicleMode[] getAllVehicleModes();

        /**
        * Retrieves the parameters property of the connected drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Parameters getParameters();

        /**
        * Retrieves the speed property of the connected drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Speed getSpeed();

        /**
        * Retrieves the attitude property of the connected drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Attitude getAttitude();

        /**
        * Retrieves the home property of the connected drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Home getHome();

        /**
        * Retrieves the battery property of the connected drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Battery getBattery();

        /**
        * Retrieves the altitude property of the connected drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Altitude getAltitude();

        /**
        * Retrieves the mission property of the connected drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Mission getMission();

        /**
        * Checks if we have access to the connected drone.
        * @param droneId id of the drone whose access to check.
        */
        boolean isConnected();

        /*** Oneway method calls ***/

        /**
        * Change the vehicle mode for the connected drone.
        * @param newMode new vehicle mode.
        */
        oneway void changeVehicleMode(in VehicleMode newMode);

        /**
        * Asynchronous call used to stop listening to updates for the drone instance whose id is
        * connected by 'droneId'.
        * If no other clients is listening to the drone instance, connection with the underlying
        * vehicle will be broken.
        */
        oneway void disconnectFromDrone();

        /**
        * Refresh the parameters for the connected drone.
        */
        oneway void refreshParameters();

        /**
        * Write the given parameters to the connected drone.
        */
        oneway void writeParameters(in Parameters parameters);

        /**
        * Upload the given mission to the connected drone.
        * @param mission mission to upload to the drone.
        */
        oneway void sendMission(in Mission mission);
}
