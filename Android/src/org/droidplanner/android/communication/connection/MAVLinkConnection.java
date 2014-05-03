package org.droidplanner.android.communication.connection;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.droidplanner.android.utils.file.FileStream;

import android.util.Log;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.MAVLink.Parser;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;

public abstract class MAVLinkConnection extends Thread {

	private static final String TAG = MAVLinkConnection.class.getSimpleName();

	protected abstract void openConnection() throws UnknownHostException,
			IOException;

	protected abstract void readDataBlock() throws IOException;

	protected abstract void sendBuffer(byte[] buffer) throws IOException;

	protected abstract void closeConnection() throws IOException;

	protected abstract void getPreferences(SharedPreferences prefs);

	public interface MavLinkConnectionListener {
		public void onReceiveMessage(MAVLinkMessage msg);

		public void onDisconnect();

		public void onComError(String errMsg);

	}

	protected Context parentContext;
	private MavLinkConnectionListener listener;
	private boolean logEnabled;

	private boolean liveUploadEnabled;
	private DroneshareClient uploader = null;
	private String droneshareLogin, dronesharePassword;

	private BufferedOutputStream logWriter;

	protected MAVLinkPacket receivedPacket;
	protected Parser parser = new Parser();
	protected byte[] readData = new byte[4096];
	protected int iavailable, i;
	protected boolean connected = true;

	private ByteBuffer logBuffer;

	public MAVLinkConnection(Context parentContext) {
		this.parentContext = parentContext;
		this.listener = (MavLinkConnectionListener) parentContext;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(parentContext);
		logEnabled = prefs.getBoolean("pref_mavlink_log_enabled", false);
		liveUploadEnabled = prefs.getBoolean("pref_live_upload_enabled", false);
		droneshareLogin = prefs.getString("dshare_username", "").trim();
		dronesharePassword = prefs.getString("dshare_password", "").trim();
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

			if (liveUploadEnabled && !droneshareLogin.isEmpty()
					&& !dronesharePassword.isEmpty()) {
				Log.i(TAG, "Starting live upload");
				uploader = new DroneshareClient();
				uploader.connect(droneshareLogin, dronesharePassword);
			} else {
				Log.w(TAG, "Skipping live upload");
			}

			while (connected) {
				readDataBlock();
				handleData();
			}

		} catch (FileNotFoundException e) {
			listener.onComError(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			listener.onComError(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (uploader != null)
					uploader.close();

				closeConnection();
			} catch (IOException e) {
				// Ignore errors while closing
			}
		}

		listener.onDisconnect();
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
				listener.onReceiveMessage(msg);
			}
		}
	}

	private void saveToLog(MAVLinkPacket receivedPacket) throws IOException {
		if (logEnabled) {
			try {
				logBuffer.clear();
				long time = System.currentTimeMillis() * 1000;
				logBuffer.putLong(time);
				byte[] bytes = receivedPacket.encodePacket();
				logWriter.write(logBuffer.array());
				logWriter.write(bytes);

				// HUGE FIXME - it is possible for the current filterMavlink to
				// block BAD-BAD
				if (uploader != null)
					uploader.filterMavlink(uploader.interfaceNum, bytes);
			} catch (Exception e) {
				// There was a null pointer error for some users on
				// logBuffer.clear();
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
			listener.onComError(e.getMessage());
			e.printStackTrace();
		}
	}

	public void disconnect() {
		connected = false;
	}

}
