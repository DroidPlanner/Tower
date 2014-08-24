package org.droidplanner.android.communication.connection;

import java.util.Map.Entry;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import org.droidplanner.core.MAVLink.connection.MavLinkConnectionTypes;

public abstract class UsbConnection extends AndroidMavLinkConnection {
	protected static int baud_rate = 57600;

	protected UsbConnection(Context parentContext) {
		super(parentContext);
	}

	@Override
	protected void loadPreferences(SharedPreferences prefs) {
		String baud_type = prefs.getString("pref_baud_type", "57600");
		if (baud_type.equals("38400"))
			baud_rate = 38400;
		else if (baud_type.equals("57600"))
			baud_rate = 57600;
		else
			baud_rate = 115200;
	}

	public static UsbConnection getUSBConnection(Context context) {
		if (isFTDIdevice(context)) {
			return new UsbFTDIConnection(context);
		} else {
			return new UsbCDCConnection(context);
		}
	}

	private static boolean isFTDIdevice(Context context) {
		UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		for (Entry<String, UsbDevice> device : manager.getDeviceList().entrySet()) {
			if (device.getValue().getVendorId() == 0x0403) {
				return true;
			}
		}
		return false;
	}

    @Override
    public int getConnectionType(){
        return MavLinkConnectionTypes.MAVLINK_CONNECTION_USB;
    }
}
