package org.droidplanner.android.dialogs;

import org.droidplanner.android.activities.interfaces.PhysicalDeviceEvents;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.InputDevice;
import android.view.MotionEvent;

public class UninterruptingDialog extends ProgressDialog {

	PhysicalDeviceEvents gcListener;
	
	public UninterruptingDialog(Context context) {
		super(context);
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) != 0) {
			if (gcListener != null)
				gcListener.physicalJoyMoved(event);
			return true;
		}
		return super.onGenericMotionEvent(event);
	}

	public void registerPhysicalDeviceEventsListener(PhysicalDeviceEvents listener) {
		this.gcListener = listener;
	}
}
