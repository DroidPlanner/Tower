package org.droidplanner.android.utils.rc.input;

import android.view.MotionEvent;

public class AxisFinder {
    private static int mFoundAxis;
    private static final int[] axis_id = {
            MotionEvent.AXIS_X, MotionEvent.AXIS_Y, // Left Stick
            MotionEvent.AXIS_RX, MotionEvent.AXIS_RY, // Right Stick
            MotionEvent.AXIS_Z, MotionEvent.AXIS_RZ, // Ouya Right Stick
            MotionEvent.AXIS_LTRIGGER, MotionEvent.AXIS_RTRIGGER
    };
    private static float[] initialValues = null;
    public static boolean figureOutAxis(MotionEvent event) {
        if(initialValues == null) { //Set initial Values
            initialValues = new float[axis_id.length];
            
            for (int x = 0; x < axis_id.length; x++) {
                initialValues[x] = event.getAxisValue(axis_id[x]);
            }
        }
        
        for (int x = 0; x < axis_id.length; x++) {
            float value = event.getAxisValue(axis_id[x]);
            if (Math.abs(value - initialValues[x]) > 0.4f) {
                mFoundAxis = axis_id[x];
                return true;
            }
        }
        return false;
    }

    public static int getFiguredOutAxis() {
        cancel();
        return mFoundAxis;
    }
    
    public static void cancel() {
        initialValues = null;
    }
}
