package org.droidplanner.android.communication.connection.usb;

import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

class UsbFTDIConnection extends UsbConnection.UsbConnectionImpl {

	private static final String TAG = UsbFTDIConnection.class.getSimpleName();

	private static final byte LATENCY_TIMER = 32;

	private FT_Device ftDev;

	protected UsbFTDIConnection(Context context, int baudRate) {
		super(context, baudRate);
	}

	@Override
	protected void openUsbConnection() throws IOException {
		D2xxManager ftD2xx = null;
		try {
			ftD2xx = D2xxManager.getInstance(mContext);
		} catch (D2xxManager.D2xxException ex) {
			mLogger.logErr(TAG, ex);
		}

		if (ftD2xx == null) {
			throw new IOException("Unable to retrieve D2xxManager instance.");
		}

		int DevCount = ftD2xx.createDeviceInfoList(mContext);
		Log.d(TAG, "Found " + DevCount + " ftdi devices.");
		if (DevCount < 1) {
			throw new IOException("No Devices found");
		}

		try {
			// FIXME: The NPE is coming from the library. Investigate if it's
			// possible to fix there.
			ftDev = ftD2xx.openByIndex(mContext, 0);
		} catch (NullPointerException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			if (ftDev == null) {
				throw new IOException("No Devices found");
			}
		}

		Log.d("USB", "Opening using Baud rate " + mBaudRate);
		ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
		ftDev.setBaudRate(mBaudRate);
		ftDev.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8, D2xxManager.FT_STOP_BITS_1,
				D2xxManager.FT_PARITY_NONE);
		ftDev.setFlowControl(D2xxManager.FT_FLOW_NONE, (byte) 0x00, (byte) 0x00);
		ftDev.setLatencyTimer(LATENCY_TIMER);
		ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));

		if (!ftDev.isOpen()) {
			throw new IOException();
		} else {
			Log.d("USB", "COM open");
		}
	}

	@Override
	protected int readDataBlock(byte[] readData) throws IOException {
		if (ftDev == null) {
			throw new IOException("Device is unavailable.");
		}

		int iavailable = ftDev.getQueueStatus();
		if (iavailable > 0) {
			if (iavailable > 4096)
				iavailable = 4096;
			try {
				ftDev.read(readData, iavailable);

			} catch (NullPointerException e) {
				final String errorMsg = "Error Reading: " + e.getMessage()
						+ "\nAssuming inaccessible USB device.  Closing connection.";
				Log.e(TAG, errorMsg, e);
				throw new IOException(errorMsg, e);
			}
		}

		if (iavailable == 0) {
			iavailable = -1;
		}
		return iavailable;
	}

	@Override
	protected void sendBuffer(byte[] buffer) {
		if (ftDev != null) {
			try {
				ftDev.write(buffer);
			} catch (Exception e) {
				Log.e("USB", "Error Sending: " + e.getMessage(), e);
			}
		}
	}

	@Override
	protected void closeUsbConnection() throws IOException {
		if (ftDev != null) {
			try {
				ftDev.close();
			} catch (Exception e) {
				// Ignore.
			}
			ftDev = null;
		}
	}

	@Override
	public String toString() {
		return TAG;
	}
}
