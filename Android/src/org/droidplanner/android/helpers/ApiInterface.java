package org.droidplanner.android.helpers;

import org.droidplanner.android.api.services.DroidPlannerApi;

/**
 * Set of interfaces related to the droidplanner api.
 */
public class ApiInterface {
    /**
     * Provide access to a drone object.
     */
    public static interface Provider {
        DroidPlannerApi getApi();
    }

    /**
     * Implement if interested in api connection status notification.
     */
    public static interface Subscriber {

        void onApiConnected(DroidPlannerApi api);

        void onApiDisconnected();
    }
}
