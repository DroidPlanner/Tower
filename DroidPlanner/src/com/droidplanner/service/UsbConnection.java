package com.droidplanner.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

public abstract class UsbConnection extends MAVLinkConnection {
	private D2xxManager ftD2xx;
	private FT_Device ftDev;

	public UsbConnection(Context parentContext) {
		super(parentContext);
		openCOM();
		getUSBDriver();
	}

	@Override
	public void run() {
		super.run();

		while (true) {
			iavailable = ftDev.getQueueStatus();

			if (iavailable > 0) {
				if (iavailable > 4096)
					iavailable = 4096;
				ftDev.read(readData, iavailable);
			}
			
			handleData();
		}

	}

	public void sendBuffer(byte[] buffer) {
		if (ftDev.isOpen()) {
			ftDev.write(buffer);
		}
	}

	private void openCOM() {
		D2xxManager ftD2xx = null;
		try {
			ftD2xx = D2xxManager.getInstance(parentContext);
		} catch (D2xxManager.D2xxException ex) {
			ex.printStackTrace();
		}
		this.ftD2xx = ftD2xx;

	}

	private void getUSBDriver() {

		int DevCount = ftD2xx.createDeviceInfoList(parentContext);
		if (DevCount < 1) {
			Log.d("USB", "No Devices");
			return;
		}
		Log.d("USB", "Nº dev:" + DevCount);

		ftDev = ftD2xx.openByIndex(parentContext, 0);

		if (ftDev == null) {
			Log.d("USB", "COM close");
			return;
		}

		ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
		ftDev.setBaudRate(57600);
		ftDev.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8,
				D2xxManager.FT_STOP_BITS_1, D2xxManager.FT_PARITY_NONE);
		ftDev.setFlowControl(D2xxManager.FT_FLOW_NONE, (byte) 0x00, (byte) 0x00);
		ftDev.setLatencyTimer((byte) 16);
		ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));

		if (true == ftDev.isOpen()) {
			Log.d("USB", "COM open");
		}
	}
	
	public void getPreferences(SharedPreferences prefs) {		
	}

}
