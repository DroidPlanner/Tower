package org.droidplanner.android.activities.helpers;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.droidplanner.android.activities.interfaces.PhysicalDeviceEvents;
import org.droidplanner.android.utils.rc.RCConstants;

public class ControllerEventCaptureView extends View
{
    private PhysicalDeviceEvents gcListener;
    private boolean blockInput = false;
    
    public ControllerEventCaptureView(Context context) {
        super(context);
    }
    
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (RCConstants.isPhysicalDeviceEvent(event)) {
            if (gcListener != null)
                gcListener.physicalJoyMoved(event);
            
            return blockInput;
        }
        return super.onGenericMotionEvent(event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(RCConstants.isPhysicalDeviceKeyCode(event)) {
            if(gcListener != null)
                gcListener.physicalKeyUp(keyCode, event);
            
            return blockInput;
        }
        return super.onKeyUp(keyCode, event);
    }
    
    public void registerPhysicalDeviceEventListener(PhysicalDeviceEvents gcListener) {
        this.gcListener = gcListener;
    }
    
    public void blockInput(boolean block) {
        blockInput = block;
    }
}
