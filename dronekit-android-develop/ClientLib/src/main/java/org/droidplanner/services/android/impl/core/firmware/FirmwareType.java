package org.droidplanner.services.android.impl.core.firmware;

import com.MAVLink.enums.MAV_AUTOPILOT;

public enum FirmwareType {
    /* APM firmware types */
    ARDU_PLANE(MAV_AUTOPILOT.MAV_AUTOPILOT_ARDUPILOTMEGA, "ArduPlane", "ArduPlane"),
    ARDU_COPTER(MAV_AUTOPILOT.MAV_AUTOPILOT_ARDUPILOTMEGA, "ArduCopter2", "ArduCopter"),
    ARDU_ROVER(MAV_AUTOPILOT.MAV_AUTOPILOT_ARDUPILOTMEGA, "ArduRover", "ArduRover"),
    ARDU_SOLO(MAV_AUTOPILOT.MAV_AUTOPILOT_ARDUPILOTMEGA, "ArduCopter2", "ArduSolo"),

    /**
     * PX4 firmware type
     */
    PX4_NATIVE(MAV_AUTOPILOT.MAV_AUTOPILOT_PX4, "", "PX4 Native"),

    /**
     * Generic firmware type
     */
    GENERIC(MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC, "", "Generic");

    private final String type;
    private final int family;
    private final String parameterMetadataGroup;

    FirmwareType(int family, String group, String type) {
        this.family = family;
        this.type = type;
        this.parameterMetadataGroup = group;
    }

    public String getType() {
        return type;
    }

    public int getFamily() {
        return family;
    }

    public String getParameterMetadataGroup() {
        return parameterMetadataGroup;
    }

    @Override
    public String toString() {
        return type;
    }
}
