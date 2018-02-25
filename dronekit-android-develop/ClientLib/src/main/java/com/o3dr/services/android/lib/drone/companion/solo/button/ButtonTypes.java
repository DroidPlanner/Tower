package com.o3dr.services.android.lib.drone.companion.solo.button;

/**
 * Created by djmedina on 4/15/15.
 */
public class ButtonTypes {

    public static final int MESSAGE_LENGTH = 12;

    /**
     * Button IDs
     */
    public static final int BUTTON_POWER = 0;
    public static final int BUTTON_FLY = 1;
    public static final int BUTTON_RTL = 2;
    public static final int BUTTON_LOITER = 3;
    public static final int BUTTON_A = 4;
    public static final int BUTTON_B = 5;
    public static final int BUTTON_PRESET_1 = 6;
    public static final int BUTTON_PRESET_2 = 7;
    public static final int BUTTON_CAMERA_CLICK = 8;

    /**
     * Event types
     */
    public static final int BUTTON_EVENT_PRESS = 0;
    public static final int BUTTON_EVENT_RELEASE = 1;
    public static final int BUTTON_EVENT_CLICK_RELEASE = 2;
    public static final int BUTTON_EVENT_HOLD = 3;
    public static final int BUTTON_EVENT_LONG_HOLD = 4;
    public static final int BUTTON_EVENT_DOUBLE_CLICK = 5;

    //Private constructor to prevent instantiation
    private ButtonTypes(){};

}
