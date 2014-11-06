package com.ox3dr.services.android.lib.drone.mission.item;

/**
 * /**
 * List of mission item types.
 */
public class MissionItemType {

    public static final int INVALID_TYPE = -1;

    public static final int CAMERA_TRIGGER = 0;
    public static final int RAW_MESSAGE = 1;

    public static final int CHANGE_SPEED = 2;
    public static final int EPM_GRIPPER = 3;
    public static final int RETURN_TO_LAUNCH = 4;
    public static final int SET_SERVO = 5;
    public static final int TAKEOFF = 6;
    public static final int CIRCLE = 7;
    public static final int LAND = 8;
    public static final int REGION_OF_INTEREST = 9;
    public static final int SPLINE_WAYPOINT = 10;
    public static final int STRUCTURE_SCANNER = 11;
    public static final int WAYPOINT = 12;
}
