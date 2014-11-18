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
    * Acquire an handle to the droidplanner api.
    * @return IDroidPlannerApi object used to interact with the drone.
    */
    IDroidPlannerApi acquireDroidPlannerApi();

    /**
    * Release the handle to the droidplanner api.
    *
    * @param callback callback used to receive droidplanner api events.
    */
    void releaseDroidPlannerApi(IDroidPlannerApi droidplannerApi);

}
