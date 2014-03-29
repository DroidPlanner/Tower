package org.droidplanner.android.communication.connection;

import java.io.IOException;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

/**
 * This version is modified by Helibot to use the "USB Serial for Android Library"
//See https://code.google.com/p/usb-serial-for-android/ 
// It should allow support of FDTI and other Serial to USB converters.
// It should allow APM v2.0 and 2.5 to connect via USB cable straight to APM.
// Be sure to set the Telementry speed in the setting menu to 
//    115200 when connecting directly with USB cable.
*/
public class UsbConnection extends MAVLinkConnection {
	private static int baud_rate = 57600;
	private static UsbSerialDriver sDriver = null;

	public UsbConnection(Context parentContext) {
		super(parentContext);
	}

	@Override
	protected void openConnection() throws UnknownHostException, IOException {
		openCOM();
	}

	@Override
	protected void readDataBlock() throws IOException {
		// Read data from driver. This call will return upto readData.length
		// bytes.
		// If no data is received it will timeout after 200ms (as set by
		// parameter 2)
		iavailable = sDriver.read(readData, 200);
		if (iavailable == 0)
			iavailable = -1;
		// Log.d("USB", "Bytes read" + iavailable);
	}

	@Override
	protected void sendBuffer(byte[] buffer) {
		// Write data to driver. This call should write buffer.length bytes
		// if data cant be sent , then it will timeout in 500ms (as set by
		// parameter 2)
		if (connected && sDriver != null) {
			try {
				sDriver.write(buffer, 500);
			} catch (IOException e) {
				Log.e("USB", "Error Sending: " + e.getMessage(), e);
			}
		}
	}

	@Override
	protected void closeConnection() throws IOException {
		if (sDriver != null) {
			try {
				sDriver.close();
			} catch (IOException e) {
				// Ignore.
			}
			sDriver = null;
		}
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

	private void openCOM() throws IOException {
		// Get UsbManager from Android.
		UsbManager manager = (UsbManager) parentContext
				.getSystemService(Context.USB_SERVICE);

		// Find the first available driver.
		// **TODO: We should probably step through all available USB Devices
		// ...but its unlikely to happen on a Phone/tablet running DroidPlanner.
		sDriver = UsbSerialProber.findFirstDevice(manager);

		if (sDriver == null) {
			Log.d("USB", "No Devices found");
			throw new IOException("No Devices found");
		} else {
			Log.d("USB", "Opening using Baud rate " + baud_rate);
			try {
				sDriver.open();
				sDriver.setParameters(baud_rate, 8, UsbSerialDriver.STOPBITS_1,
						UsbSerialDriver.PARITY_NONE);
			} catch (IOException e) {
				Log.e("USB", "Error setting up device: " + e.getMessage(), e);
				try {
					sDriver.close();
				} catch (IOException e2) {
					// Ignore.
				}
				sDriver = null;
				return;
			}
		}
	}
}
