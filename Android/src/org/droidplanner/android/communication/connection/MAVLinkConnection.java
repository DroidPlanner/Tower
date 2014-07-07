package org.droidplanner.android.communication.connection;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.droidplanner.android.communication.service.UploaderService;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.file.FileStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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

	private DroneshareClient uploader = null;
	private DroidPlannerPrefs prefs;

	private File logFile = null;
	private BufferedOutputStream logWriter = null;

	protected MAVLinkPacket receivedPacket;
	protected Parser parser = new Parser();
	protected byte[] readData = new byte[4096];
	protected int iavailable, i;
	protected boolean connected = true;

	private ByteBuffer logBuffer;

	public MAVLinkConnection(Context parentContext) {
		this.parentContext = parentContext;
		this.listener = (MavLinkConnectionListener) parentContext;

		prefs = new DroidPlannerPrefs(parentContext);
		getPreferences(prefs.prefs);
	}

	@Override
	public void run() {
		super.run();
		try {
			parser.stats.mavlinkResetStats();
			openConnection();

            //Start a new ga analytics session. The new session will be tagged with the mavlink
            // connection mechanism, as well as whether the user has an active droneshare account.
            GAUtils.startNewSession(parentContext);

			logFile = FileStream.getTLogFile();
			logWriter = FileStream.openOutputStream(logFile);
			logBuffer = ByteBuffer.allocate(4* Long.SIZE / Byte.SIZE);
			logBuffer.order(ByteOrder.BIG_ENDIAN);
			
			String login = prefs.getDroneshareLogin();
			String password = prefs.getDronesharePassword();
			if (prefs.getLiveUploadEnabled() && !login.isEmpty()
					&& !password.isEmpty()) {
				Log.i(TAG, "Starting live upload");
				uploader = new DroneshareClient();
				uploader.connect(login, password);
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
			if (connected) { // Ignore errors while shutting down
				listener.onComError(e.getMessage());
				e.printStackTrace();
			}
		} finally {
			try {
				// Never leak file descriptors
				if (logWriter != null) {
					// Success - commit our tlog!
					logWriter.close();
					logWriter = null;

					FileStream.commitFile(logFile);

					// See if we can at least do a delayed upload
					parentContext.startService(UploaderService
							.createIntent(parentContext));
				}

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
		if (logWriter != null) {
			try {
				logBuffer.clear();
				long time = System.currentTimeMillis() * 1000;
				logBuffer.putLong(time);
				byte[] bytes = receivedPacket.encodePacket();
				logWriter.write(logBuffer.array());
				logWriter.write(bytes);

				if (uploader != null)
					uploader.filterMavlink(uploader.interfaceNum, bytes);
			} catch (IOException e) {
				Log.e(TAG, "Ignoring IO error in saveToLog: " + e);
			}catch (BufferOverflowException e) {
				Log.e(TAG, "Ignoring Buffer Overflow in saveToLog: " + e);
			} catch (NullPointerException e) {
				Log.e(TAG, "Ignoring NPE in " + e);
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
		Log.i(TAG, "Disconnecting mavlink");
		connected = false;

		// Force our socket or file descriptor closed, which will cause
		// readDataBlock() to return.
		try {
			closeConnection();
		} catch (IOException ex) {
			// If connection already closed, succeed silently
		}
	}

}
