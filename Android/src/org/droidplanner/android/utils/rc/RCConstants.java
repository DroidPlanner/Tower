package org.droidplanner.android.utils.rc;

import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class RCConstants {
    public static final int AILERON = 0;
    public static final int ELEVATOR = 1;
    public static final int THROTTLE = 2;
    public static final int RUDDER = 3;

    public static final int RC5 = 4;
    public static final int RC6 = 5;
    public static final int RC7 = 6;
    public static final int RC8 = 7;

    public static final int[] rchannels = {
            AILERON, ELEVATOR, THROTTLE, RUDDER
            // TODO Add the rest after testing
    };
    public static final String[] STRINGRCCHANNELS = {
            "AILERON", "ELEVATOR", "THROTTLE", "RUDDER"
            // TODO Add the rest after testing
    };

    public final static int MODE_SINGLEKEY = 0;
    public final static int MODE_INCREMENTKEY = 1;
    public final static int MODE_DECREMENTKEY = 2;
    public static boolean isPhysicalDeviceEvent(MotionEvent event) {
        int sources = event.getSource();
        boolean isJoystick = (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                || ((sources & InputDevice.SOURCE_JOYSTICK)
                == InputDevice.SOURCE_JOYSTICK));
        boolean isTrigger = event.getAxisValue(11) < 0.5f || event.getAxisValue(14) < 0.5f; // Ouya left and right triggers
        return isJoystick || isTrigger;

    }
    public static boolean isPhysicalDeviceEvent(KeyEvent event) {
        return event.getKeyCode() == 110; // For now, find better way, probably using device id
    }
}
