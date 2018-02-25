package com.o3dr.services.android.lib.drone.companion.solo.tlv;

/**
 * All sololink messages types.
 */
public class TLVMessageTypes {

    public static final int TYPE_SOLO_MESSAGE_GET_CURRENT_SHOT = 0;
    public static final int TYPE_SOLO_MESSAGE_SET_CURRENT_SHOT = 1;
    public static final int TYPE_SOLO_MESSAGE_LOCATION = 2;
    public static final int TYPE_SOLO_MESSAGE_RECORD_POSITION = 3;
    public static final int TYPE_SOLO_CABLE_CAM_OPTIONS = 4;
    public static final int TYPE_SOLO_GET_BUTTON_SETTING = 5;
    public static final int TYPE_SOLO_SET_BUTTON_SETTING = 6;
    public static final int TYPE_SOLO_PAUSE_BUTTON = 7;

    public static final int TYPE_SOLO_FOLLOW_OPTIONS = 19;
    public static final int TYPE_SOLO_FOLLOW_OPTIONS_V2 = 119;
    public static final int TYPE_SOLO_SHOT_OPTIONS = 20;
    public static final int TYPE_SOLO_SHOT_ERROR = 21;

    public static final int TYPE_SOLO_PANO_OPTIONS = 22;
    public static final int TYPE_SOLO_ZIPLINE_OPTIONS = 23;
    public static final int TYPE_SOLO_REWIND_OPTIONS = 24;
    public static final int TYPE_SOLO_PANO_STATUS = 25;
    public static final int TYPE_RTL_HOME_POINT = 26;
    public static final int TYPE_SOLO_POWER_STATE = 27;
    public static final int TYPE_SOLO_ZIPLINE_LOCK = 28;

    public static final int TYPE_SOLO_MESSAGE_SHOT_MANAGER_ERROR = 1000;
    public static final int TYPE_SOLO_CABLE_CAM_WAYPOINT = 1001;

    public static final int TYPE_ARTOO_INPUT_REPORT_MESSAGE = 2003;

    public static final int TYPE_SOLO_GOPRO_SET_REQUEST = 5001;
    public static final int TYPE_SOLO_GOPRO_RECORD = 5003;
    public static final int TYPE_SOLO_GOPRO_STATE = 5005;
    public static final int TYPE_SOLO_GOPRO_STATE_V2 = 5006;
    public static final int TYPE_SOLO_GOPRO_REQUEST_STATE = 5007;
    public static final int TYPE_SOLO_GOPRO_SET_EXTENDED_REQUEST = 5009;

    /*
    Multi point cable cam message types
     */
    public static final int TYPE_SOLO_SPLINE_RECORD = 50;
    public static final int TYPE_SOLO_SPLINE_PLAY = 51;
    public static final int TYPE_SOLO_SPLINE_POINT = 52;
    public static final int TYPE_SOLO_SPLINE_SEEK = 53;
    public static final int TYPE_SOLO_SPLINE_PLAYBACK_STATUS = 54;
    public static final int TYPE_SOLO_SPLINE_PATH_SETTINGS = 55;
    public static final int TYPE_SOLO_SPLINE_DURATIONS = 56;
    public static final int TYPE_SOLO_SPLINE_ATTACH = 57;

    /*
    Site scan inspect shot message types
     */
    public static final int TYPE_SOLO_INSPECT_START = 10001;
    public static final int TYPE_SOLO_INSPECT_SET_WAYPOINT = 10002;
    public static final int TYPE_SOLO_INSPECT_MOVE_GIMBAL = 10003;
    public static final int TYPE_SOLO_INSPECT_MOVE_VEHICLE = 10004;

    /*
    Site scan scan shot message types
     */
    public static final int TYPE_SOLO_SCAN_START = 10101;

    /*
    Site scan survey shot message types
     */
    public static final int TYPE_SOLO_SURVEY_START = 10201;

    //Private constructor to prevent instantiation
    private TLVMessageTypes(){}
}
