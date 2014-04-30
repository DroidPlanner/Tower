package org.droidplanner.android.helpers;

import android.util.Log;
import android.view.InputDevice;

public class ExternalJoystick {
	public void printInputDevicesToLog() {
		int[] inputIds = InputDevice.getDeviceIds();
		Log.d("DEV", "Found " + inputIds.length);
		for (int i = 0; i < inputIds.length; i++) {
			InputDevice inputDevice = InputDevice.getDevice(inputIds[i]);
			Log.d("DEV", "name:" + inputDevice.getName() + " Sources:"
					+ inputDevice.getSources());
		}
	}
}
