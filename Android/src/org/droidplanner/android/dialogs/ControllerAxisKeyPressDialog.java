package org.droidplanner.android.dialogs;

import org.droidplanner.android.utils.rc.RCConstants;
import org.droidplanner.android.utils.rc.input.AxisFinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class ControllerAxisKeyPressDialog extends ProgressDialog {

    public interface ControllerPressListener
    {
        void onControllerPress(int id, int key);
    }

    private ControllerPressListener listener;
    private int id = 0;
    
    public ControllerAxisKeyPressDialog(Context context) {
        super(context);
        Initialize();
    }
    public void setId(int id) {
        this.id = id;
    }
    private void Initialize() {
        setTitle("Waiting for input...");
        setIndeterminate(true);
        setMessage("Move Joystick to autodetect");
        setCancelable(true);
        setButton(ProgressDialog.BUTTON_NEUTRAL, "Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });
    }

    public void setJoystickMode() {
        setMessage("Move Joystick to autodetect");
    }
    public void setButtonMode() {
        setMessage("Press button to autodetect");
    }
    
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (RCConstants.isPhysicalDeviceEvent(event) && AxisFinder.figureOutAxis(event)) {
            listener.onControllerPress(id, AxisFinder.getFiguredOutAxis());
            dismiss();
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (RCConstants.isPhysicalDeviceKeyCode(event)) {
            listener.onControllerPress(id, event.getKeyCode());
            dismiss();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void registerListener(ControllerPressListener listener) {
        this.listener = listener;
    }
}
