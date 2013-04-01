package com.droidplanner.service;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.MAVLink.Parser;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.droidplanner.helpers.FileManager;

public abstract class MAVLinkConnection extends Thread {
	public abstract void onReceiveMessage(MAVLinkMessage msg);

	public abstract void sendBuffer(byte[] buffer);

	public abstract void getPreferences(SharedPreferences prefs);

	public abstract void onDisconnect();

	public abstract void onConnect();

	protected Context parentContext;

	protected boolean logEnabled;
	private BufferedOutputStream logWriter;

	MAVLinkMessage m;
	Parser parser = new Parser();
	byte[] readData = new byte[4096];
	int iavailable, i;

	public MAVLinkConnection(Context parentContext) {
		this.parentContext = parentContext;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(parentContext);
		logEnabled = prefs.getBoolean("pref_mavlink_log_enabled", false);
		getPreferences(prefs);
	}

	@Override
	public void run() {
		super.run();
		try {
			if (logEnabled) {
				logWriter = FileManager.getTLogFileStream();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected void handleData() {
		if (iavailable < 1) {
			return;
		}
		for (i = 0; i < iavailable; i++) {
			m = parser.mavlink_parse_char(readData[i] & 0x00ff);
			if (m != null) {
				onReceiveMessage(m);
			}
		}

		if (logEnabled) {
			try {
				logWriter.write(readData, 0, iavailable);
			} catch (IOException e) {
				e.printStackTrace();
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
		sendBuffer(buffer);
	}


}
