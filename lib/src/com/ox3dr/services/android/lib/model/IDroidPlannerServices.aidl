// IDroidPlannerServices.aidl
package com.ox3dr.services.android.lib.model;

import com.ox3dr.services.android.lib.model.ITLogApi;
import com.ox3dr.services.android.lib.model.IDroidPlannerApiCallback;
import com.ox3dr.services.android.lib.model.IDroidPlannerApi;
import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;

/**
* Used to establish connection with a drone.
*/
interface IDroidPlannerServices {
    /**
    * Used to register with a drone instance whose connection parameters match the passed one.
    * The 'callback' argument must be non-null, and allows to receive updates about the
    * drone instance.
    *
    * @param params ConnectionParameter object
    * @param callback Client callback instance
    * @return IDroidPlannerApi object used to interact with the drone.
    */
    IDroidPlannerApi registerWithDrone(in ConnectionParameter params, IDroidPlannerApiCallback callback);

    /**
    * Used to unregsiter from the drone instance whose connection parameters match the passed one.
    * @param params ConnectionParameter object
    * @param callback client callback instance.
    */
    void unregisterFromDrone(in ConnectionParameter params, IDroidPlannerApiCallback callback);

    /**
    * Get access to the tlog api.
    */
    ITLogApi getTLogApi();
}
