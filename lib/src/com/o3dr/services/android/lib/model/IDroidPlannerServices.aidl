// IDroidPlannerServices.aidl
package com.o3dr.services.android.lib.model;

import com.o3dr.services.android.lib.model.IDroidPlannerApi;

/**
* Used to establish connection with a drone.
*/
interface IDroidPlannerServices {

    /**
    * Ping the 3DR Services to make sure it's still up and connected.
    */
    boolean ping();

    /**
    * Retrieve an handle to the droidplanner api.
    *
    * @param tag used to retrieve an existing handle if it exists.
    * @return IDroidPlannerApi object used to interact with the drone.
    */
    IDroidPlannerApi getDroidPlannerApi(String tag);

}
