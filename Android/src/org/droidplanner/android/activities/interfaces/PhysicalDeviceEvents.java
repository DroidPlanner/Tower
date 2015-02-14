package org.droidplanner.android.activities.interfaces;

import android.view.KeyEvent;
import android.view.MotionEvent;

public interface PhysicalDeviceEvents {
    
    void physicalJoyMoved(MotionEvent event);

    void physicalKeyUp(int keyCode, KeyEvent event);

}
