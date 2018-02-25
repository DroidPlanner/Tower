package org.droidplanner.services.android.impl.core.MAVLink.connection;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.Pair;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.util.UriUtils;

import org.droidplanner.services.android.impl.core.model.Logger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base for mavlink connection implementations.
 */
public abstract class MavLinkConnection {

    private static final String TAG = MavLinkConnection.class.getSimpleName();

    /*
     * MavLink connection states
     */
    public static final int MAVLINK_DISCONNECTED = 0;
    public static final int MAVLINK_CONNECTING = 1;
    public static final int MAVLINK_CONNECTED = 2;

    /**
     * Size of the buffer used to read messages from the mavlink connection.
     */
    private static final int READ_BUFFER_SIZE = 4096;

    /**
     * @see {@link android.net.Network}
     */
    public static final String EXTRA_NETWORK = "extra_network";

    /**
     * Set of listeners subscribed to this mavlink connection. We're using a
     * ConcurrentSkipListSet because the object will be accessed from multiple
     * threads concurrently.
     */
    private final ConcurrentHashMap<String, MavLinkConnectionListener> mListeners = new ConcurrentHashMap<>();

    /**
     * Stores the list of log files to be written to.
     */
    private final ConcurrentHashMap<String, Pair<Uri, BufferedOutputStream>> loggingOutStreams = new
        ConcurrentHashMap<>();

    /**
     * Queue the set of packets to send via the mavlink connection. A thread
     * will be blocking on it until there's element(s) available to send.
     */
    private final LinkedBlockingQueue<byte[]> mPacketsToSend = new LinkedBlockingQueue<>();

    /**
     * Queue the set of packets to log. A thread will be blocking on it until
     * there's element(s) available for logging.
     */
    private final LinkedBlockingQueue<byte[]> mPacketsToLog = new LinkedBlockingQueue<>();

    private final AtomicInteger mConnectionStatus = new AtomicInteger(MAVLINK_DISCONNECTED);
    private final AtomicLong mConnectionTime = new AtomicLong(-1);
    private final AtomicReference<Bundle> extrasHolder = new AtomicReference<>();

    /**
     * Start the connection process.
     */
    private final Runnable mConnectingTask = new Runnable() {
        @Override
        public void run() {
            // Load the connection specific preferences
            loadPreferences();
            // Open the connection
            try {
                openConnection(extrasHolder.get());
            } catch (IOException e) {
                // Ignore errors while shutting down
                if (mConnectionStatus.get() != MAVLINK_DISCONNECTED) {
                    reportIOException(e);

                    mLogger.logErr(TAG, e);
                }

                disconnect();
            }

            mLogger.logInfo(TAG, "Exiting connecting thread.");
        }
    };

    @LinkConnectionStatus.FailureCode
    private int getErrorCode(IOException e) {
        if (e instanceof BindException) {
            return LinkConnectionStatus.ADDRESS_IN_USE;
        } else {
            return LinkConnectionStatus.UNKNOWN;
        }
    }

    /**
     * Manages the receiving and sending of messages.
     */
    private final Runnable mManagerTask = new Runnable() {

        @Override
        public void run() {
            Thread sendingThread = null;
            Thread loggingThread = null;

            try {
                final long connectionTime = System.currentTimeMillis();
                mConnectionTime.set(connectionTime);
                reportConnect(connectionTime);

                // Launch the 'Sending' thread
                mLogger.logInfo(TAG, "Starting sender thread.");
                sendingThread = new Thread(mSendingTask, "MavLinkConnection-Sending Thread");
                sendingThread.start();

                //Launch the 'Logging' thread
                mLogger.logInfo(TAG, "Starting logging thread.");
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
                // Ignore errors while shutting down
                if (mConnectionStatus.get() != MAVLINK_DISCONNECTED) {
                    reportIOException(e);
                    mLogger.logErr(TAG, e);
                }
            } finally {
                if (sendingThread != null && sendingThread.isAlive()) {
                    sendingThread.interrupt();
                }

                if (loggingThread != null && loggingThread.isAlive()) {
                    loggingThread.interrupt();
                }

                disconnect();
                mLogger.logInfo(TAG, "Exiting manager thread.");
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
                    reportReceivedPacket(receivedPacket);
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
            try {
                while (mConnectionStatus.get() == MAVLINK_CONNECTED) {
                    byte[] buffer = mPacketsToSend.take();

                    try {
                        sendBuffer(buffer);
                        queueToLog(buffer);
                    } catch (IOException e) {
                        reportIOException(e);
                        mLogger.logErr(TAG, e);
                    }
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
            final ByteBuffer logBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
            logBuffer.order(ByteOrder.BIG_ENDIAN);

            try {
                while (mConnectionStatus.get() == MAVLINK_CONNECTED) {

                    final byte[] packetData = mPacketsToLog.take();

                    logBuffer.clear();
                    logBuffer.putLong(System.currentTimeMillis() * 1000);

                    for (Map.Entry<String, Pair<Uri, BufferedOutputStream>> entry : loggingOutStreams
                        .entrySet()) {
                        final Pair<Uri, BufferedOutputStream> logInfo = entry.getValue();
                        final Uri loggingFileUri = logInfo.first;
                        try {
                            BufferedOutputStream logWriter = logInfo.second;
                            if (logWriter == null) {
                                logWriter = new BufferedOutputStream(UriUtils.getOutputStream(context, loggingFileUri));
                                loggingOutStreams.put(entry.getKey(), Pair.create(loggingFileUri, logWriter));
                            }

                            logWriter.write(logBuffer.array());
                            logWriter.write(packetData);
                        } catch (IOException e) {
                            mLogger.logErr(TAG, "IO Exception while writing to " + loggingFileUri, e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                final String errorMessage = e.getMessage();
                if (errorMessage != null) {
                    mLogger.logVerbose(TAG, errorMessage);
                }
            } finally {
                for (Pair<Uri, BufferedOutputStream> entry : loggingOutStreams.values()) {
                    final Uri loggingFileUri = entry.first;
                    try {
                        if (entry.second != null) {
                            entry.second.close();
                        }
                    } catch (IOException e) {
                        mLogger.logErr(TAG, "IO Exception while closing " + loggingFileUri, e);
                    }
                }

                loggingOutStreams.clear();
            }
        }
    };

    protected final Logger mLogger = initLogger();
    protected final Context context;

    private Thread mConnectThread;
    private Thread mTaskThread;

    protected MavLinkConnection(Context context){
        this.context = context;
    }

    public void connect(Bundle extras) {
        if (mConnectionStatus.compareAndSet(MAVLINK_DISCONNECTED, MAVLINK_CONNECTING)) {
            extrasHolder.set(extras);
            mLogger.logInfo(TAG, "Starting connection thread.");
            mConnectThread = new Thread(mConnectingTask, "MavLinkConnection-Connecting Thread");
            mConnectThread.start();
            reportConnecting();
        }
    }

    protected void onConnectionOpened(Bundle extras) {
        if (mConnectionStatus.compareAndSet(MAVLINK_CONNECTING, MAVLINK_CONNECTED)) {
            extrasHolder.set(extras);
            mLogger.logInfo(TAG, "Starting manager thread.");
            mTaskThread = new Thread(mManagerTask, "MavLinkConnection-Manager Thread");
            mTaskThread.start();
        }
    }

    protected void onConnectionStatus(LinkConnectionStatus connectionStatus) {
        reportConnectionStatus(connectionStatus);

        switch (connectionStatus.getStatusCode()) {
            case LinkConnectionStatus.FAILED:
                mLogger.logInfo(TAG, "Unable to establish connection: " + connectionStatus.getStatusCode());
                disconnect();
                break;
        }
    }

    /**
     * Disconnect a mavlink connection. If the operation is successful, it will
     * be reported through the MavLinkConnectionListener interface.
     */
    public void disconnect() {
        if (mConnectionStatus.get() == MAVLINK_DISCONNECTED || (mConnectThread == null && mTaskThread == null)) {
            return;
        }

        try {
            mConnectionStatus.set(MAVLINK_DISCONNECTED);
            mConnectionTime.set(-1);
            extrasHolder.set(null);

            if (mConnectThread != null && mConnectThread.isAlive() && !mConnectThread.isInterrupted()) {
                mConnectThread.interrupt();
            }

            if (mTaskThread != null && mTaskThread.isAlive() && !mTaskThread.isInterrupted()) {
                mTaskThread.interrupt();
            }

            closeConnection();
            reportDisconnect();
        } catch (IOException e) {
            mLogger.logErr(TAG, e);
            reportIOException(e);
        }
    }

    public int getConnectionStatus() {
        return mConnectionStatus.get();
    }

    public void sendMavPacket(MAVLinkPacket packet) {
        final byte[] packetData = packet.encodePacket();
        if (!mPacketsToSend.offer(packetData)) {
            mLogger.logErr(TAG, "Unable to send mavlink packet. Packet queue is full!");
        }
    }

    private void queueToLog(MAVLinkPacket packet) {
        if (packet != null) {
            queueToLog(packet.encodePacket());
        }
    }

    private void queueToLog(byte[] packetData) {
        if (packetData != null) {
            if (!mPacketsToLog.offer(packetData)) {
                mLogger.logErr(TAG, "Unable to log mavlink packet. Queue is full!");
            }
        }
    }

    public void addLoggingPath(String tag, Uri loggingUri) {
        if (tag == null || tag.length() == 0 || loggingUri == null) {
            return;
        }

        if (!loggingOutStreams.contains(tag)) {
            loggingOutStreams.put(tag, Pair.<Uri, BufferedOutputStream>create(loggingUri, null));
        }
    }

    public void removeLoggingPath(String tag) {
        if (tag == null || tag.length() == 0) {
            return;
        }

        Pair<Uri, BufferedOutputStream> logInfo = loggingOutStreams.remove(tag);
        if (logInfo != null) {
            BufferedOutputStream outStream = logInfo.second;
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    mLogger.logErr(TAG, "IO Exception while closing " + logInfo.first, e);
                }
            }
        }
    }

    /**
     * Adds a listener to the mavlink connection.
     *
     * @param listener
     * @param tag      Listener tag
     */
    public void addMavLinkConnectionListener(String tag, MavLinkConnectionListener listener) {
        mListeners.put(tag, listener);

        if (getConnectionStatus() == MAVLINK_CONNECTED) {
            Bundle extras = new Bundle();
            extras.putLong(LinkConnectionStatus.EXTRA_CONNECTION_TIME, mConnectionTime.get());
            listener.onConnectionStatus(new LinkConnectionStatus(LinkConnectionStatus.CONNECTED, extras));
        }
    }

    /**
     * @return the count of connection listeners.
     */
    public int getMavLinkConnectionListenersCount() {
        return mListeners.size();
    }

    public Bundle getConnectionExtras() {
        return extrasHolder.get();
    }

    /**
     * Used to query the presence of a connection listener.
     *
     * @param tag connection listener tag
     * @return true if the tag is present in the listeners list.
     */
    public boolean hasMavLinkConnectionListener(String tag) {
        return mListeners.containsKey(tag);
    }

    /**
     * Removes the specified listener.
     *
     * @param tag Listener tag
     */
    public void removeMavLinkConnectionListener(String tag) {
        mListeners.remove(tag);
    }

    /**
     * Removes all the connection listeners.
     */
    public void removeAllMavLinkConnectionListeners() {
        mListeners.clear();
    }

    protected abstract Logger initLogger();

    protected abstract void openConnection(Bundle connectionExtras) throws IOException;

    protected abstract int readDataBlock(byte[] buffer) throws IOException;

    protected abstract void sendBuffer(byte[] buffer) throws IOException;

    protected abstract void closeConnection() throws IOException;

    protected abstract void loadPreferences();

    /**
     * @return The type of this mavlink connection.
     */
    public abstract int getConnectionType();

    protected Logger getLogger() {
        return mLogger;
    }

    /**
     * Utility method to notify the mavlink listeners about communication
     * errors.
     *
     * @param connectionStatus
     */
    protected void reportConnectionStatus(LinkConnectionStatus connectionStatus) {
        if (mListeners.isEmpty()) {
            return;
        }

        for (MavLinkConnectionListener listener : mListeners.values()) {
            listener.onConnectionStatus(connectionStatus);
        }
    }

    protected void reportConnecting() {
        reportConnectionStatus(new LinkConnectionStatus(LinkConnectionStatus.CONNECTING, null));
    }

    /**
     * Utility method to notify the mavlink listeners about a successful
     * connection.
     */
    protected void reportConnect(long connectionTime) {
        Bundle extras = new Bundle();
        extras.putLong(LinkConnectionStatus.EXTRA_CONNECTION_TIME, connectionTime);
        reportConnectionStatus(new LinkConnectionStatus(LinkConnectionStatus.CONNECTED, extras));
    }

    /**
     * Utility method to notify the mavlink listeners about a connection
     * disconnect.
     */
    protected void reportDisconnect() {
        reportConnectionStatus(new LinkConnectionStatus(LinkConnectionStatus.DISCONNECTED, null));
    }

    /**
     * Utility method to notify the mavlink listeners about received messages.
     *
     * @param packet received mavlink packet
     */
    private void reportReceivedPacket(MAVLinkPacket packet) {
        if (mListeners.isEmpty()) {
            return;
        }

        for (MavLinkConnectionListener listener : mListeners.values()) {
            listener.onReceivePacket(packet);
        }
    }

    protected void reportIOException(IOException e) {
        reportConnectionStatus(LinkConnectionStatus.newFailedConnectionStatus(getErrorCode(e), e.getMessage()));
    }
}
