package org.droidplanner.core.MAVLink.connection;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Parser;

import org.droidplanner.core.model.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base for mavlink connection implementations.
 */
public abstract class MavLinkConnection {

    private static final String TAG = MavLinkConnection.class.getSimpleName();

    /*
    MavLink connection states
     */
    public static final int MAVLINK_DISCONNECTED = 0;
    public static final int MAVLINK_CONNECTING = 1;
    public static final int MAVLINK_CONNECTED = 2;

    /**
     * Size of the buffer used to read messages from the mavlink connection.
     */
    private static final int READ_BUFFER_SIZE = 4096;

    /**
     * Maximum possible sequence number for a packet.
     */
    private static final int MAX_PACKET_SEQUENCE = 255;

    /**
     * Set of listeners subscribed to this mavlink connection.
     * We're using a ConcurrentSkipListSet because the object will be accessed from multiple
     * threads concurrently.
     */
    private final ConcurrentSkipListSet<MavLinkConnectionListener> mListeners = new
            ConcurrentSkipListSet<MavLinkConnectionListener>();

    /**
     * Queue the set of packets to log. A thread will be blocking on it until there's element(s)
     * available for logging.
     */
    private final LinkedBlockingQueue<MAVLinkPacket> mPacketsToLog = new
            LinkedBlockingQueue<MAVLinkPacket>();

    /**
     * Queue the set of packets to send via the mavlink connection. A thread will be blocking on
     * it until there's element(s) available to send.
     */
    private final LinkedBlockingQueue<MAVLinkPacket> mPacketsToSend = new
            LinkedBlockingQueue<MAVLinkPacket>();

    private final AtomicInteger mConnectionStatus = new AtomicInteger(MAVLINK_DISCONNECTED);

    /**
     * Listen for incoming data on the mavlink connection.
     */
    private final Runnable mConnectingTask = new Runnable() {

        @Override
        public void run() {
            Thread sendingThread = null, loggingThread = null;

            //Load the connection specific preferences
            loadPreferences();

            try {
                //Open the connection
                openConnection();
                mConnectionStatus.set(MAVLINK_CONNECTED);
                reportConnect();

                //Launch the 'Sending', and 'Logging' threads
                sendingThread = new Thread(mSendingTask, "MavLinkConnection-Sending Thread");
                sendingThread.start();

                loggingThread = new Thread(mLoggingTask, "MavLinkConnection-Logging Thread");
                loggingThread.start();

                final Parser parser = new Parser();
                parser.stats.mavlinkResetStats();

                final byte[] readBuffer = new byte[READ_BUFFER_SIZE];

                while (mConnectionStatus.get() == MAVLINK_CONNECTED) {
                    int bufferSize = readDataBlock(readBuffer);
                    handleData(parser, bufferSize, readBuffer);
                }
            } catch (IOException e) {
                //Ignore errors while shutting down
                if (mConnectionStatus.get() == MAVLINK_CONNECTED) {
                    reportComError(e.getMessage());
                    mLogger.logErr(TAG, e);
                }
            } finally {
                if (loggingThread != null && loggingThread.isAlive()) {
                    loggingThread.interrupt();
                }

                if (sendingThread != null && sendingThread.isAlive()) {
                    sendingThread.interrupt();
                }

                disconnect();
            }
        }

        private void handleData(Parser parser, int bufferSize, byte[] buffer) {
            if (bufferSize < 1) {
                return;
            }

            for (int i = 0; i < bufferSize; i++) {
                MAVLinkPacket receivedPacket = parser.mavlink_parse_char(buffer[i] & 0x00ff);
                if (receivedPacket != null) {
                    queueToLog(receivedPacket);
                    MAVLinkMessage msg = receivedPacket.unpack();
                    reportReceivedMessage(msg);
                }
            }
        }
    };

    /**
     * Blocks until there's packet(s) to send, then dispatch them.
     */
    private final Runnable mSendingTask = new Runnable() {
        @Override
        public void run() {
            int msgSeqNumber = 0;

            try {
                while (mConnectionStatus.get() == MAVLINK_CONNECTED) {
                    final MAVLinkPacket packet = mPacketsToSend.take();
                    packet.seq = msgSeqNumber;
                    byte[] buffer = packet.encodePacket();

                    try {
                        sendBuffer(buffer);
                        queueToLog(packet);
                    } catch (IOException e) {
                        reportComError(e.getMessage());
                        mLogger.logErr(TAG, e);
                    }

                    msgSeqNumber = (msgSeqNumber + 1) % (MAX_PACKET_SEQUENCE + 1);
                }
            } catch (InterruptedException e) {
                mLogger.logVerbose(TAG, e.getMessage());
            } finally {
                disconnect();
            }
        }
    };

    /**
     * Blocks until there's packets to log, then dispatch them.
     */
    private final Runnable mLoggingTask = new Runnable() {
        @Override
        public void run() {
            final File tmpLogFile = getTempTLogFile();
            final ByteBuffer logBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
            logBuffer.order(ByteOrder.BIG_ENDIAN);

            try {
                final BufferedOutputStream logWriter = new BufferedOutputStream(new
                        FileOutputStream(tmpLogFile));
                try {
                    while (true) {
                        final MAVLinkPacket packet = mPacketsToLog.take();

                        logBuffer.clear();
                        logBuffer.putLong(System.currentTimeMillis() * 1000);

                        logWriter.write(logBuffer.array());
                        logWriter.write(packet.encodePacket());

                        onLogSaved(packet);
                    }
                } catch (InterruptedException e) {
                    mLogger.logVerbose(TAG, e.getMessage());
                } finally {
                    logWriter.close();

                    //Commit the tlog file.
                    commitTempTLogFile(tmpLogFile);
                }
            } catch (FileNotFoundException e) {
                mLogger.logErr(TAG, e);
                reportComError(e.getMessage());
            } catch (IOException e) {
                mLogger.logErr(TAG, e);
                reportComError(e.getMessage());
            }
        }
    };

    protected final Logger mLogger = initLogger();

    private Thread mTaskThread;

    /**
     * Establish a mavlink connection.
     * If the connection is successful, it will be reported through the
     * MavLinkConnectionListener interface.
     */
    public void connect() {
        if (mConnectionStatus.compareAndSet(MAVLINK_DISCONNECTED, MAVLINK_CONNECTING)) {
            mTaskThread = new Thread(mConnectingTask, "MavLinkConnection-Connecting Thread");
            mTaskThread.start();
        }
    }

    /**
     * Disconnect a mavlink connection.
     * If the operation is successful, it will be reported through the MavLinkConnectionListener
     * interface.
     */
    public void disconnect() {
        if (mConnectionStatus.get() == MAVLINK_DISCONNECTED || mTaskThread == null) {
            return;
        }

        try {
            closeConnection();
            mConnectionStatus.set(MAVLINK_DISCONNECTED);
            mTaskThread.interrupt();
            reportDisconnect();
        } catch (IOException e) {
            mLogger.logErr(TAG, e);
            reportComError(e.getMessage());
        }
    }

    public int getConnectionStatus() {
        return mConnectionStatus.get();
    }

    public void sendMavPacket(MAVLinkPacket packet){
        if(!mPacketsToSend.offer(packet)){
            mLogger.logErr(TAG, "Unable to send mavlink packet. Packet queue is full!");
        }
    }

    /**
     * Adds a listener to the mavlink connection.
     *
     * @param listener
     */
    public void addMavLinkConnectionListener(MavLinkConnectionListener listener) {
        mListeners.add(listener);
    }

    /**
     * Removes the specified listener.
     *
     * @param listener
     */
    public void removeMavLinkConnectionListener(MavLinkConnectionListener listener) {
        mListeners.remove(listener);
    }

    protected abstract Logger initLogger();

    protected abstract void openConnection() throws IOException;

    protected abstract int readDataBlock(byte[] buffer) throws IOException;

    protected abstract void sendBuffer(byte[] buffer) throws IOException;

    protected abstract void closeConnection() throws IOException;

    protected abstract void loadPreferences();

    protected abstract File getTempTLogFile();

    protected abstract void commitTempTLogFile(File tlogFile);

    /**
     * @return The type of this mavlink connection.
     */
    public abstract int getConnectionType();

    /**
     * Overrides if interested in being notified when the log is written.
     * Note: Is called from a background thred.
     *
     * @param packet MAVLinkPacket saved to log.
     */
    protected void onLogSaved(MAVLinkPacket packet) throws IOException {
    }

    protected Logger getLogger(){
        return mLogger;
    }

    /**
     * Queue a mavlink packet for logging.
     *
     * @param packet MAVLinkPacket packet
     * @return true if the packet was queued successfully.
     */
    private boolean queueToLog(MAVLinkPacket packet) {
        return mPacketsToLog.offer(packet);
    }

    /**
     * Utility method to notify the mavlink listeners about communication errors.
     *
     * @param errMsg
     */
    private void reportComError(String errMsg) {
        for (MavLinkConnectionListener listener : mListeners) {
            listener.onComError(errMsg);
        }
    }

    /**
     * Utility method to notify the mavlink listeners about a successful connection.
     */
    private void reportConnect() {
        for (MavLinkConnectionListener listener : mListeners) {
            listener.onConnect();
        }
    }

    /**
     * Utility method to notify the mavlink listeners about a connection disconnect.
     */
    private void reportDisconnect() {
        for (MavLinkConnectionListener listener : mListeners) {
            listener.onDisconnect();
        }
    }

    /**
     * Utility method to notify the mavlink listeners about received messages.
     *
     * @param msg received mavlink message
     */
    private void reportReceivedMessage(MAVLinkMessage msg) {
        for (MavLinkConnectionListener listener : mListeners) {
            listener.onReceiveMessage(msg);
        }
    }

}
