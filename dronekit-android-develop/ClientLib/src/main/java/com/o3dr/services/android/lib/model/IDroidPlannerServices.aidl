// IDroidPlannerServices.aidl
package com.o3dr.services.android.lib.model;

import com.o3dr.services.android.lib.model.IDroneApi;
import com.o3dr.services.android.lib.model.IApiListener;

/**
* Used to establish connection with a drone.
*/
interface IDroidPlannerServices {

    /**
    * Retrieve the version code for the 3DR Service implementation.
    */
    int getServiceVersionCode();

    /**
    * Release the handle to the droidplanner api.
    *
    * @param callback callback used to receive drone api events.
    */
    void releaseDroneApi(IDroneApi droneApi);

    /**
    * Retrieve the version code for the api.
    */
    int getApiVersionCode();

    /**
    * Acquire an handle to the droidplanner api.
    * @param listener listener for the DroneAPI events.
    * @param appId application id for the application acquiring the drone api handle.
    * @return IDroneApi object used to interact with the drone.
    */
    IDroneApi registerDroneApi(IApiListener listener, String appId);

    /**
    * Retrieve the list of all the connected apps.
    * The bundles in the returned array contains the appId and connection parameter of the connected apps.
    * @param requesterId id for the application requesting the information.
    */
    Bundle[] getConnectedApps(String requesterId);
}
