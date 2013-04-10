package com.droidplanner.service;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.MAVLink.Parser;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.droidplanner.helpers.FileManager;

public abstract class MAVLinkConnection extends Thread {

	protected abstract void openConnection() throws UnknownHostException, IOException;
	protected abstract void readDataBlock() throws IOException;
	protected abstract void sendBuffer(byte[] buffer) throws IOException;
	protected abstract void closeConnection() throws IOException;
	protected abstract void getPreferences(SharedPreferences prefs);

	public interface MavLinkConnectionListner{
		public void onReceiveMessage(MAVLinkMessage msg);
		public void onDisconnect();
		public void onConnect();
	}
	
	protected Context parentContext;
	private MavLinkConnectionListner listner;
	private boolean logEnabled;
	private BufferedOutputStream logWriter;

	protected MAVLinkPacket receivedPacket;
	protected Parser parser = new Parser();
	protected byte[] readData = new byte[4096];
	protected int iavailable, i;
	protected boolean connected = true;
	
	public MAVLinkConnection(Context parentContext) {
		this.parentContext = parentContext;
		this.listner = (MavLinkConnectionListner) parentContext;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(parentContext);
		logEnabled = prefs.getBoolean("pref_mavlink_log_enabled", false);
		getPreferences(prefs);
	}

	@Override
	public void run() {
		super.run();
		try {
			openConnection();
			if (logEnabled) {
				logWriter = FileManager.getTLogFileStream();
			}
			listner.onConnect();
			
			while (connected) {
				readDataBlock();
				handleData();
				saveToLog();
			}	
			closeConnection();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		listner.onDisconnect();
	}

	
	
	private void handleData() {
		if (iavailable < 1) {
			return;
		}
		for (i = 0; i < iavailable; i++) {
			receivedPacket = parser.mavlink_parse_char(readData[i] & 0x00ff);
			if (receivedPacket != null) {
				MAVLinkMessage msg = receivedPacket.unpack();
				listner.onReceiveMessage(msg);
			}
		}

	}
	
	private void saveToLog() throws IOException {
		if (logEnabled) {
			logWriter.write(readData, 0, iavailable);
		}
	}

	/**
	 * Format and send a Mavlink packet via the MAVlink stream
	 * 
	 * @param packet
	 *            MavLink packet to be transmitted
	 */
	public void sendMavPacket(MAVLinkPacket packet) {
		byte[] buffer = packet.encodePacket();
		try {
			sendBuffer(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect(){
		connected = false;
	}



}
