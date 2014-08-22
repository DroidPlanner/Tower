package com.droidplanner.connection;

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
import com.droidplanner.file.FileStream;

public abstract class MAVLinkConnection extends Thread {

	protected abstract void openConnection() throws UnknownHostException,
			IOException;

	protected abstract void readDataBlock() throws IOException;

	protected abstract void sendBuffer(byte[] buffer) throws IOException;

	protected abstract void closeConnection() throws IOException;

	protected abstract void getPreferences(SharedPreferences prefs);

	public interface MavLinkConnectionListner {
		public void onReceiveMessage(MAVLinkMessage msg);

		public void onDisconnect();
	}

	protected Context parentContext;
	private MavLinkConnectionListner listner;
	private boolean logEnabled;
	private BufferedOutputStream logWriter;
	
	// Maintain MAVLink sequence number for all packets we send here
	private int msg_seq_number;

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
			parser.stats.mavlinkResetStats(); 
			openConnection();
			if (logEnabled) {
				logWriter = FileStream.getTLogFileStream();
				logBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
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
				// There was a null pointer error for some users on
				// logBuffer.clear();
			}
		}
	}

	/**
	 * Format and send a MAVLink packet via the MAVLink stream
	 * 
	 * @param packet
	 *            MavLink packet to be transmitted
	 */
	public void sendMavPacket(MAVLinkPacket packet) {
		
		/* Set the correct sequence number in the packet before we send it
		 * 
		 * This will send 1 as the sequence number for the first packet instead of 0
		 * Despite this, it will wrap correctly to 0 once it reaches 255
		 */
		
		msg_seq_number++;
		
		// Check if we will overflow the max value in a byte, if so set to 0
		if (msg_seq_number > 255){
			
			// Set to 0 to prevent overflowing maximum value of a byte
			msg_seq_number = 0;
		}
		packet.seq = msg_seq_number;
		
		byte[] buffer = packet.encodePacket();
		try {
			sendBuffer(buffer);
			saveToLog(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		connected = false;
	}

}
