package org.droidplanner.android.communication.connection;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class UsbConnection extends MAVLinkConnection {
	protected static int baud_rate = 57600;

	protected UsbConnection(Context parentContext) {
		super(parentContext);
	}

	@Override
	protected void getPreferences(SharedPreferences prefs) {
		String baud_type = prefs.getString("pref_baud_type", "57600");
		if (baud_type.equals("38400"))
			baud_rate = 38400;
		else if (baud_type.equals("57600"))
			baud_rate = 57600;
		else
			baud_rate = 115200;
	}

	public static MAVLinkConnection getUSBConnection(Context context) {
		if (isFTDIdevice()) {
			return new UsbFTDIConnection(context);
		} else {
			return new UsbCDCConnection(context);
		}
	}

	private static boolean isFTDIdevice() {
		return false;
	}
}
