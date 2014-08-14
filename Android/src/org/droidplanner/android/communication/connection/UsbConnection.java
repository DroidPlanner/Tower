package org.droidplanner.android.communication.connection;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

public class UsbConnection extends MAVLinkConnection {
	private static int baud_rate = 57600;

	private static final byte LATENCY_TIMER = 32;

	private FT_Device ftDev;

	public UsbConnection(Context parentContext) {
		super(parentContext);
	}

	@Override
	protected void openConnection() throws IOException {
		openCOM();
	}

	@Override
	protected void readDataBlock() throws IOException {
		iavailable = ftDev.getQueueStatus();
		if (iavailable > 0) {
			if (iavailable > 4096)
				iavailable = 4096;
			try {
				ftDev.read(readData, iavailable);

			} catch (NullPointerException e) {
				Log.e("USB", "Error Reading: " + e.getMessage()
						+ "\nAssuming inaccessible USB device.  Closing connection.", e);
				closeConnection();
			}
		}

		if (iavailable == 0) {
			iavailable = -1;
		}
	}

	@Override
	protected void sendBuffer(byte[] buffer) {
		if (connected & ftDev != null) {
			try {
				ftDev.write(buffer);
			} catch (Exception e) {
				Log.e("USB", "Error Sending: " + e.getMessage(), e);
			}
		}
	}

	@Override
	protected void closeConnection() throws IOException {
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

		D2xxManager ftD2xx = null;
		try {
			ftD2xx = D2xxManager.getInstance(parentContext);
		} catch (D2xxManager.D2xxException ex) {
			ex.printStackTrace();
		}

		int DevCount = ftD2xx.createDeviceInfoList(parentContext);
		if (DevCount < 1) {
			throw new IOException("No Devices found");
		}

		ftDev = ftD2xx.openByIndex(parentContext, 0);

		if (ftDev == null) {
			throw new IOException("No Devices found");
		}

		Log.d("USB", "Opening using Baud rate " + baud_rate);
		ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
		ftDev.setBaudRate(baud_rate);
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
}
