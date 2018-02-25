package org.droidplanner.services.android.impl.core.drone.autopilot.apm;

/**
 * Set of APM autopilots related constants.
 * Created by Fredia Huya-Kouadio on 7/28/15.
 */
public class APMConstants {

    /**
     * Index of the home waypoint within a mission items list.
     */
    public static final int HOME_WAYPOINT_INDEX = 0;

    //Private to prevent instantiation
    private APMConstants(){}

    /**
     * Severity levels used in STATUSTEXT messages
     */
    public static class Severity {
        public static final int SEVERITY_LOW= 1;
        public static final int SEVERITY_MEDIUM = 2;
        public static final int SEVERITY_HIGH = 3;
        public static final int SEVERITY_CRITICAL = 4;
        public static final int SEVERITY_USER_RESPONSE = 5;
    };
}
