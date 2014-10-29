// IDroidPlannerReaderApi.aidl
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
interface IDroidPlannerReaderApi {

    /**
    * Retrieves the gps property of the specified drone.
    * @param droneId id of the drone whose property to retrieve.
    */
    Gps getGps(int droneId);

    /**
    * Retrieves the state property of the specified drone.
    * @param droneId id of the drone whose property to retrieve.
    */
    State getState(int droneId);

    /**
    * Retrieves the parameters property of the specified drone.
    * @param droneId id of the drone whose property to retrieve.
    */
    Parameters getParameters(int droneId);

    /**
    * Retrieves the speed property of the specified drone.
    * @param droneId id of the drone whose property to retrieve.
    */
    Speed getSpeed(int droneId);

    /**
    * Retrieves the attitude property of the specified drone.
    * @param droneId id of the drone whose property to retrieve.
    */
    Attitude getAttitude(int droneId);

    /**
    * Retrieves the home property of the specified drone.
    * @param droneId id of the drone whose property to retrieve.
    */
    Home getHome(int droneId);

    /**
    * Retrieves the battery property of the specified drone.
    * @param droneId id of the drone whose property to retrieve.
    */
    Battery getBattery(int droneId);

    /**
    * Retrieves the altitude property of the specified drone.
    * @param droneId id of the drone whose property to retrieve.
    */
    Altitude getAltitude(int droneId);

    /**
    * Retrieves the mission property of the specified drone.
    * @param droneId id of the drone whose property to retrieve.
    */
    Mission getMission(int droneId);
}
