package org.droidplanner.android.communication.connection;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.droidplanner.android.communication.service.UploaderService;
import org.droidplanner.android.utils.DroidplannerPrefs;
import org.droidplanner.android.utils.file.FileStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.MAVLink.Parser;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;

/**
 * This class holds the logic to instantiate, communicate over,
 * and close a mavlink data connection.
 * @since 1.2.0
 */
public abstract class MAVLinkConnection extends Thread {

    /**
     * This tag is used for logging.
     */
    private static final String TAG = MAVLinkConnection.class.getSimpleName();

	protected abstract void openConnection() throws UnknownHostException,
			IOException;

	protected abstract void readDataBlock() throws IOException;

	protected abstract void sendBuffer(byte[] buffer) throws IOException;

	protected abstract void closeConnection() throws IOException;

	protected abstract void getPreferences(SharedPreferences prefs);

	public interface MavLinkConnectionListener {

        /**
         * This method is called by the mavlink connection when a successful connection is
         * established.
         * @since 1.2.0
         */
        public void onConnect();

		public void onReceiveMessage(MAVLinkPacket msgPacket);

		public void onDisconnect();
		
		public void onComError(String errMsg);		
		
	}

	protected Context parentContext;
	private MavLinkConnectionListener listener;

	private DroneshareClient uploader = null;
	private DroidplannerPrefs prefs;

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

		prefs = new DroidplannerPrefs(parentContext);
		getPreferences(prefs.prefs);
	}

	@Override
	public void run() {
		super.run();
		try {
			parser.stats.mavlinkResetStats();
			openConnection();

            //If we get here, the connection is successful. Notify the listener
            listener.onConnect();
            
			if (prefs.getLogEnabled()) {
				logFile = FileStream.getTLogFile();
				logWriter = FileStream.openOutputStream(logFile);
				logBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
				logBuffer.order(ByteOrder.BIG_ENDIAN);
			}

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
        MAVLinkPacket[] receivedPackets = parseMavlinkBuffer(readData, iavailable);
		if (receivedPackets == null) {
			return;
		}

        for(MAVLinkPacket receivedPacket: receivedPackets){
			if (receivedPacket != null) {
				saveToLog(receivedPacket);
				listener.onReceiveMessage(receivedPacket);
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

    /**
     * Parse the received byte(s) into mavlink packets.
     * @param mavlinkBuffer received byte(s) buffer
     * @param numBytes bytes count
     * @return parsed mavlink packets
     */
    public MAVLinkPacket[] parseMavlinkBuffer(byte[] mavlinkBuffer, int numBytes){
        return parseMavlinkBuffer(parser, mavlinkBuffer, numBytes);
    }

    /**
     * Parse the received byte(s) into mavlink packets.
     * @param parser
     * @param mavlinkBuffer received byte(s) buffer
     * @param numBytes bytes count
     * @return parsed mavlink packets
     */
    public static MAVLinkPacket[] parseMavlinkBuffer(Parser parser, byte[] mavlinkBuffer,
                                                     int numBytes){
        if(numBytes < 1)
            return null;

        MAVLinkPacket[] parsedPackets = new MAVLinkPacket[numBytes];
        for(int i = 0; i < numBytes; i++){
            parsedPackets[i] = parser.mavlink_parse_char(mavlinkBuffer[i] & 0x00ff);
        }
        return parsedPackets;
    }

}
