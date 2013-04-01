package com.droidplanner.service;

import java.io.IOException;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

public class UsbConnection extends MAVLinkConnection {
	private static final int BAUD_RATE = 57600;
	private static final byte LATENCY_TIMER = 16;
	
	private FT_Device ftDev;

	public UsbConnection(Context parentContext) {
		super(parentContext);
	}

	@Override
	protected void openConnection() throws UnknownHostException, IOException {
		openCOM();
	}

	@Override
	protected void readDataBlock() throws IOException {
		iavailable = ftDev.getQueueStatus();
		if (iavailable > 0) {
			if (iavailable > 4096)
				iavailable = 4096;
			ftDev.read(readData, iavailable);
		}		
	}

	@Override
	protected void sendBuffer(byte[] buffer) {
		if (connected & ftDev !=null) {
			ftDev.write(buffer);
		}
	}
	
	@Override
	protected void closeConnection() throws IOException {
		ftDev.close();
	}
	
	@Override
	protected void getPreferences(SharedPreferences prefs) {		
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
			Log.d("USB", "No Devices");
			throw new IOException();
		}
		Log.d("USB", "Nº dev:" + DevCount);
		
		ftDev = ftD2xx.openByIndex(parentContext, 0);
		
		if (ftDev == null) {
			Log.d("USB", "COM close");
			throw new IOException();
		}
		
		ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
		ftDev.setBaudRate(BAUD_RATE);
		ftDev.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8,
				D2xxManager.FT_STOP_BITS_1, D2xxManager.FT_PARITY_NONE);
		ftDev.setFlowControl(D2xxManager.FT_FLOW_NONE, (byte) 0x00, (byte) 0x00);
		ftDev.setLatencyTimer(LATENCY_TIMER);
		ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
		
		if (true == ftDev.isOpen()) {
			Log.d("USB", "COM open");
		}
	}

	


}
