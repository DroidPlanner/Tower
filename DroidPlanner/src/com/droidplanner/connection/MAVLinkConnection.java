package com.droidplanner.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Parser;
import com.droidplanner.file.FileStream;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class holds the logic to instantiate, communicate over,
 * and close a mavlink data connection.
 * @since 1.2.0
 */
public abstract class MAVLinkConnection extends Thread {

    /**
     * This tag is used for logging.
     * @since 1.2.0
     */
    private static final String TAG = MAVLinkConnection.class.getName();

	protected abstract void openConnection() throws UnknownHostException,
			IOException;

	protected abstract void readDataBlock() throws IOException;

	protected abstract void sendBuffer(byte[] buffer) throws IOException;

	protected abstract void closeConnection() throws IOException;

	protected abstract void getPreferences(SharedPreferences prefs);

	public interface MavLinkConnectionListner {

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
	private MavLinkConnectionListner listner;
	private boolean logEnabled;
	private BufferedOutputStream logWriter;

	protected static Parser parser = new Parser();
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
		try {
			parser.stats.mavlinkResetStats();
			openConnection();

            //If we get here, the connection is successful. Notify the listener
            listner.onConnect();

			if (logEnabled) {
				logWriter = FileStream.getTLogFileStream();
				logBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
				logBuffer.order(ByteOrder.BIG_ENDIAN);
			}

			while (connected) {
				readDataBlock();
				handleData();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
        finally {
            try {
                //No matter what exception is thrown, the connection gets closed.
                closeConnection();
            } catch (IOException e) {
                Log.e(TAG, "Unable to close open connection.", e);
            }
        }
        listner.onDisconnect();
	}

	private void handleData() throws IOException {
        MAVLinkPacket[] receivedPackets = parseMavlinkBuffer(readData, iavailable);
		if (receivedPackets == null) {
			return;
		}

        for(MAVLinkPacket receivedPacket: receivedPackets){
			if (receivedPacket != null) {
				saveToLog(receivedPacket);
				listner.onReceiveMessage(receivedPacket);
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
            listner.onComError(e.getMessage());
            e.printStackTrace();
        }
	}

	public void disconnect() {
		connected = false;
	}

    /**
     * Parse the received byte(s) into mavlink packets.
     * @param mavlinkBuffer received byte(s) buffer
     * @param numBytes bytes count
     * @return parsed mavlink packets
     * @since 1.2.0
     */
    public static MAVLinkPacket[] parseMavlinkBuffer(byte[] mavlinkBuffer, int numBytes){
        if(numBytes < 1)
            return null;

        MAVLinkPacket[] parsedPackets = new MAVLinkPacket[numBytes];
        for(int i = 0; i < numBytes; i++){
            parsedPackets[i] = parser.mavlink_parse_char(mavlinkBuffer[i] & 0x00ff);
        }
        return parsedPackets;
    }

}
