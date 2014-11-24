package org.droidplanner.android.utils.rc.input;

import android.view.MotionEvent;

public class AxisFinder {
    private static int mFoundAxis;
    private static final int[] axis_id = {
            MotionEvent.AXIS_X, MotionEvent.AXIS_Y, // Left Stick
            MotionEvent.AXIS_RX, MotionEvent.AXIS_RY, // Right Stick
            MotionEvent.AXIS_Z, MotionEvent.AXIS_RZ, // Ouya Right Stick
            MotionEvent.AXIS_LTRIGGER, MotionEvent.AXIS_RTRIGGER
    // Left and right trigger
    };

    public static boolean figureOutAxis(MotionEvent event) {
        for (int x = 0; x < axis_id.length; x++) {
            float value = event.getAxisValue(axis_id[x]);
            if (value > 0.60f || value < -0.60f) {
                mFoundAxis = axis_id[x];
                return true;
            }
        }
        return false;
    }

    public static int getFiguredOutAxis() {
        return mFoundAxis;
    }
}
