// IDroidPlannerApi.aidl
package com.three_dr.services.android.lib.model;

import com.three_dr.services.android.lib.drone.property.Gps;
import com.three_dr.services.android.lib.drone.property.Home;
import com.three_dr.services.android.lib.drone.property.Speed;
import com.three_dr.services.android.lib.drone.property.Attitude;
import com.three_dr.services.android.lib.drone.property.Altitude;
import com.three_dr.services.android.lib.drone.property.Battery;
import com.three_dr.services.android.lib.drone.property.Parameters;
import com.three_dr.services.android.lib.drone.property.Mission;
import com.three_dr.services.android.lib.drone.property.State;

/**
* Interface used to access the drone properties.
*/
interface IDroidPlannerApi {
    /**
        * Retrieves the gps property of the specified drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Gps getGps();

        /**
        * Retrieves the state property of the specified drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        State getState();

        /**
        * Retrieves the parameters property of the specified drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Parameters getParameters();

        /**
        * Retrieves the speed property of the specified drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Speed getSpeed();

        /**
        * Retrieves the attitude property of the specified drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Attitude getAttitude();

        /**
        * Retrieves the home property of the specified drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Home getHome();

        /**
        * Retrieves the battery property of the specified drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Battery getBattery();

        /**
        * Retrieves the altitude property of the specified drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Altitude getAltitude();

        /**
        * Retrieves the mission property of the specified drone.
        * @param droneId id of the drone whose property to retrieve.
        */
        Mission getMission();

        /**
        * Checks if we have access to the specified drone.
        * @param droneId id of the drone whose access to check.
        */
        boolean isConnected();

        /*** Oneway method calls ***/

        /**
        * Change the vehicle mode for the specified drone.
        * @param newMode enum name of the new vehicle mode.
        */
        oneway void changeVehicleMode(String newModeName);

        /**
        * Asynchronous call used to stop listening to updates for the drone instance whose id is
        * specified by 'droneId'.
        * If no other clients is listening to the drone instance, connection with the underlying
        * vehicle will be broken.
        */
        oneway void disconnectFromDrone();

        /**
        * Refresh the parameters for the specified drone.
        */
        oneway void refreshParameters();

        /**
        * Write the given parameters to the specified drone.
        */
        oneway void writeParameters(in Parameters parameters);

        /**
        * Upload the given mission to the specified drone.
        * @param mission mission to upload to the drone.
        */
        oneway void sendMission(in Mission mission);
}
