package com.droidplanner.service;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.MAVLink.Parser;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.droidplanner.helpers.file.FileStream;

public abstract class MAVLinkConnection extends Thread {

	protected abstract void openConnection() throws UnknownHostException, IOException;
	protected abstract void readDataBlock() throws IOException;
	protected abstract void sendBuffer(byte[] buffer) throws IOException;
	protected abstract void closeConnection() throws IOException;
	protected abstract void getPreferences(SharedPreferences prefs);

	public interface MavLinkConnectionListner{
		public void onReceiveMessage(MAVLinkMessage msg);
		public void onDisconnect();
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
	
	private ByteBuffer logBuffer;
			  
	
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
				logWriter = FileStream.getTLogFileStream();
				logBuffer = ByteBuffer.allocate(Long.SIZE/Byte.SIZE);
				logBuffer.order(ByteOrder.BIG_ENDIAN);
			}
			
			while (connected) {
				readDataBlock();
				handleData();
			}	
			closeConnection();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		listner.onDisconnect();
	}

	
	
	private void handleData() throws IOException {
		if (iavailable < 1) {
			return;
		}
		for (i = 0; i < iavailable; i++) {
			receivedPacket = parser.mavlink_parse_char(readData[i] & 0x00ff);
			if (receivedPacket != null) {
				saveToLog(receivedPacket);
				MAVLinkMessage msg = receivedPacket.unpack();
				listner.onReceiveMessage(msg);
			}
		}

	}
	
	private void saveToLog(MAVLinkPacket receivedPacket) throws IOException {
		if (logEnabled) {
			try {
				logBuffer.clear();
				long time = System.currentTimeMillis() * 1000;
				logBuffer.putLong(time);
				logWriter.write(logBuffer.array());
				logWriter.write(receivedPacket.encodePacket());
			} catch (Exception e) {
				// There was a null pointer error for some users on logBuffer.clear(); 
			}
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
			saveToLog(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect(){
		connected = false;
	}



}
