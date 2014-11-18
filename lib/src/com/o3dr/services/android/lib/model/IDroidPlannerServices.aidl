// IDroidPlannerServices.aidl
package com.o3dr.services.android.lib.model;

import com.o3dr.services.android.lib.model.IDroidPlannerApi;
import com.o3dr.services.android.lib.model.IDroidPlannerApiCallback;

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
    *
    * @param callback callback used to receive drone events.
    * @return IDroidPlannerApi object used to interact with the drone.
    */
    IDroidPlannerApi acquireDroidPlannerApi(IDroidPlannerApiCallback callback);

    /**
    * Release the handle to the droidplanner api.
    *
    * @param callback callback used to receive droidplanner api events.
    */
    void releaseDroidPlannerApi(IDroidPlannerApiCallback callback);

}
