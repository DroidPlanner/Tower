package org.droidplanner.android.communication.connection;

import java.io.IOException;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

public class UsbCDCConnection extends UsbConnection {
	private static UsbSerialDriver sDriver = null;

	protected UsbCDCConnection(Context parentContext) {
		super(parentContext);
	}

	@Override
	protected void openConnection() throws IOException {
		// Get UsbManager from Android.
		UsbManager manager = (UsbManager) parentContext.getSystemService(Context.USB_SERVICE);

		// Find the first available driver.
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
			}
		}
	}

	@Override
	protected void readDataBlock() throws IOException {
		// Read data from driver. This call will return upto readData.length
		// bytes.
		// If no data is received it will timeout after 200ms (as set by
		// parameter 2)
		try {
			iavailable = sDriver.read(readData, 200);
		} catch (NullPointerException e) {
			Log.e("USB", "Error Reading: " + e.getMessage()
					+ "\nAssuming inaccessible USB device.  Closing connection.", e);
			closeConnection();
		}
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
}
