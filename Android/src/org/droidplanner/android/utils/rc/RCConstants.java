package org.droidplanner.android.utils.rc;

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
            AILERON, ELEVATOR, THROTTLE, RUDDER, RC5, RC6, RC7, RC8
    };
    public static final String[] RChannelsTitle = {
        "Aileron", "Elevator", "Throttle", "Rudder", "RC5", "RC6", "RC7", "RC8"
    };
    public static final String[] ShortRChannelsTitle = {
        "Ail", "Elev", "Thr", "Rudd", "RC5", "RC6", "RC7", "RC8"
    };
    
    public final static int MODE_SINGLEKEY = 0;
    public final static int MODE_INCREMENTKEY = 1;
    public final static int MODE_DECREMENTKEY = 2;
    public static final int MODE_JOYSTICK_BUTTON = 3;
    
    public static boolean isPhysicalDeviceEvent(MotionEvent event) {
        return event.getDeviceId() > 0;

    }
    public static boolean isPhysicalDeviceKeyCode(KeyEvent event) {
        return event.getDeviceId() > 0;
    }
}
