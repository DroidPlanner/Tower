package org.droidplanner.android.communication.connection.usb;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.droidplanner.android.communication.connection.AndroidMavLinkConnection;
import org.droidplanner.android.utils.AndroidLogger;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionTypes;
import org.droidplanner.core.model.Logger;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class UsbConnection extends AndroidMavLinkConnection {

	private static final String TAG = UsbConnection.class.getSimpleName();

	private static final int FTDI_DEVICE_VENDOR_ID = 0x0403;

	protected int mBaudRate = 57600;

	private UsbConnectionImpl mUsbConnection;

	public UsbConnection(Context parentContext) {
		super(parentContext);
	}

	@Override
	protected void closeAndroidConnection() throws IOException {
		if (mUsbConnection != null) {
			mUsbConnection.closeUsbConnection();
		}
	}

	@Override
	protected void loadPreferences(SharedPreferences prefs) {
		String baud_type = prefs.getString("pref_baud_type", "57600");
		if (baud_type.equals("38400"))
			mBaudRate = 38400;
		else if (baud_type.equals("57600"))
			mBaudRate = 57600;
		else
			mBaudRate = 115200;
	}

	@Override
	protected void openAndroidConnection() throws IOException {
		if (mUsbConnection != null) {
			try {
				mUsbConnection.openUsbConnection();
				Log.d(TAG, "Reusing previous usb connection.");
				return;
			} catch (IOException e) {
				Log.e(TAG, "Previous usb connection is not usable.", e);
				mUsbConnection = null;
			}
		}

		if (isFTDIdevice(mContext)) {
			final UsbConnectionImpl tmp = new UsbFTDIConnection(mContext, mBaudRate);
			try {
				tmp.openUsbConnection();

				// If the call above is successful, 'mUsbConnection' will be
				// set.
				mUsbConnection = tmp;
				Log.d(TAG, "Using FTDI usb connection.");
			} catch (IOException e) {
				Log.d(TAG, "Unable to open a ftdi usb connection. Falling back to the open "
						+ "usb-library.", e);
			}
		}

		// Fallback
		if (mUsbConnection == null) {
			final UsbConnectionImpl tmp = new UsbCDCConnection(mContext, mBaudRate);

			// If an error happens here, let it propagate up the call chain
			// since this is the
			// fallback.
			tmp.openUsbConnection();
			mUsbConnection = tmp;
			Log.d(TAG, "Using open-source usb connection.");
		}
	}

	private static boolean isFTDIdevice(Context context) {
		UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		final HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		if (deviceList == null || deviceList.isEmpty()) {
			return false;
		}

		for (Entry<String, UsbDevice> device : deviceList.entrySet()) {
			if (device.getValue().getVendorId() == FTDI_DEVICE_VENDOR_ID) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected int readDataBlock(byte[] buffer) throws IOException {
		if (mUsbConnection == null) {
			throw new IOException("Uninitialized usb connection.");
		}

		return mUsbConnection.readDataBlock(buffer);
	}

	@Override
	protected void sendBuffer(byte[] buffer) throws IOException {
		if (mUsbConnection == null) {
			throw new IOException("Uninitialized usb connection.");
		}

		mUsbConnection.sendBuffer(buffer);
	}

	@Override
	public int getConnectionType() {
		return MavLinkConnectionTypes.MAVLINK_CONNECTION_USB;
	}

	@Override
	public String toString() {
		if (mUsbConnection == null) {
			return TAG;
		}

		return mUsbConnection.toString();
	}

	static abstract class UsbConnectionImpl {
		protected final int mBaudRate;
		protected final Context mContext;
		protected final Logger mLogger = AndroidLogger.getLogger();

		protected UsbConnectionImpl(Context context, int baudRate) {
			mContext = context;
			mBaudRate = baudRate;
		}

		protected abstract void closeUsbConnection() throws IOException;

		protected abstract void openUsbConnection() throws IOException;

		protected abstract int readDataBlock(byte[] readData) throws IOException;

		protected abstract void sendBuffer(byte[] buffer);
	}
}
