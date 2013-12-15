package com.droidplanner.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Parser;
import com.droidplanner.file.FileStream;
import com.droidplanner.utils.Constants;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
		public void onReceiveMessage(MAVLinkMessage msg);

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

    /**
     * Bluetooth server to relay the mavlink packet to listening connected clients.
     *
     * @since 1.2.0
     */
    private BluetoothServer mBtServer;

    /**
     * This is the bluetooth server relay listener. Handles the messages received by the
     * bluetooth relay server by the connected client devices, and push them through the mavlink
     * connection.
     * @since 1.2.0
     */
    private BluetoothServer.RelayListener mRelayListener = new BluetoothServer.RelayListener() {
        @Override
        public void onMessageToRelay(MAVLinkPacket[] relayedPackets) {
            if (relayedPackets != null) {
                for (MAVLinkPacket packet : relayedPackets){
                    if(packet != null)
                        sendMavPacket(packet);
                }

            }
        }
    };

    /**
     * Listens to broadcast events, and appropriately enable or disable the bluetooth relay server.
     * @since 1.2.0
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(Constants.ACTION_BLUETOOTH_RELAY_SERVER.equals(action)){
                boolean isEnabled = intent.getBooleanExtra(Constants
                        .EXTRA_BLUETOOTH_RELAY_SERVER_ENABLED,
                        Constants.DEFAULT_BLUETOOTH_RELAY_SERVER_TOGGLE);

                setupBtRelayServer(isEnabled);
            }
        }
    };

	public MAVLinkConnection(Context parentContext) {
		this.parentContext = parentContext;
		this.listner = (MavLinkConnectionListner) parentContext;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(parentContext);
		logEnabled = prefs.getBoolean("pref_mavlink_log_enabled", false);
		getPreferences(prefs);
	}

    /**
     * @return true if the user has enabled the bluetooth relay server.
     * @since 1.2.0
     */
    private boolean isBtRelayServerEnabled(){
        return PreferenceManager.getDefaultSharedPreferences(parentContext).getBoolean(
                Constants.PREF_BLUETOOTH_RELAY_SERVER_TOGGLE,
                Constants.DEFAULT_BLUETOOTH_RELAY_SERVER_TOGGLE);
    }

    /**
     * Setup the bluetooth relay server based on the passed argument.
     * @param isEnabled true to initialize, and start the bluetooth relay server; false to stop it.
     * @since 1.2.0
     */
    private void setupBtRelayServer(boolean isEnabled){
        if(isEnabled){
            if(mBtServer == null)
                mBtServer = new BluetoothServer();
            mBtServer.addRelayListener(mRelayListener);
            mBtServer.start();
        }else if(mBtServer != null){
            mBtServer.stop();
            mBtServer.removeRelayListener(mRelayListener);
            mBtServer = null;
        }
    }


	@Override
	public void run() {
		try {
            //Register a broadcast event receiver in case the relay server is enabled/disabled
            // while the mavlink connection is running.
            parentContext.registerReceiver(mReceiver, new IntentFilter(Constants
                    .ACTION_BLUETOOTH_RELAY_SERVER));

			parser.stats.mavlinkResetStats(); 
			openConnection();
			if (logEnabled) {
				logWriter = FileStream.getTLogFileStream();
				logBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
				logBuffer.order(ByteOrder.BIG_ENDIAN);
			}

            setupBtRelayServer(isBtRelayServerEnabled());

			while (connected) {
				readDataBlock();
				handleData();
			}

            setupBtRelayServer(false);
            
		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
        finally {
            try {
                closeConnection();
            } catch (IOException e) {
                Log.e(TAG, "Unable to close open connection.", e);
            }
            parentContext.unregisterReceiver(mReceiver);
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
				MAVLinkMessage msg = receivedPacket.unpack();
				listner.onReceiveMessage(msg);

                if (mBtServer != null) {
                    //Send the received packet to the connected clients
                    mBtServer.relayMavPacket(receivedPacket);
                }
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
