package org.droidplanner.android.dialogs;

import org.droidplanner.android.utils.rc.RCConstants;
import org.droidplanner.android.utils.rc.input.AxisFinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class ControllerAxisKeyPressDialog extends ProgressDialog {

    private AxisFinder axisFinder;

    public interface ControllerPressListener
    {
        void onControllerPress(ControllerAxisKeyPressDialog mode, int key, boolean fromJoystick);
    }

    private ControllerPressListener listener;
    public int ID;
    
    public ControllerAxisKeyPressDialog(Context context) {
        super(context);
        Initialize();
    }
    private void Initialize() {
        setTitle("Waiting for input...");
        setIndeterminate(true);
        setMessage("Move Joystick or press button to detect");
        setCancelable(true);
        setButton(ProgressDialog.BUTTON_NEUTRAL, "Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });
        axisFinder = new AxisFinder();
    }
    
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (RCConstants.isPhysicalDeviceEvent(event) && axisFinder.figureOutAxis(event)) {
            listener.onControllerPress(this, axisFinder.getFiguredOutAxis(), true);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (RCConstants.isPhysicalDeviceKeyCode(event)) {
            listener.onControllerPress(this, keyCode, false);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void registerListener(ControllerPressListener listener) {
        this.listener = listener;
    }
}
