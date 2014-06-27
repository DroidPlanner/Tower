package org.droidplanner.android.communication.connection.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Parser;

import org.droidplanner.android.communication.connection.MAVLinkConnection;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class allows the device to serve as a bluetooth relay for the mavlink data to other
 * connected bluetooth clients (i.e: glass)
 *
 * @author Fredia Huya-Kouadio
 * @since 1.2.0
 */
public class BluetoothServer {

    /**
     * Tag used for logging.
     *
     * @since 1.2.0
     */
    private static final String TAG = BluetoothServer.class.getName();

    /**
     * Name for the SDP record when creating server socket.
     *
     * @since 1.2.0
     */
    private static final String NAME = BluetoothServer.class.getSimpleName();

    /**
     * List of 7 randomly-generated UUIDS.
     *
     * @since 1.2.0
     */
    static final UUID[] UUIDS = {
            UUID.fromString("51f88040-5ea2-11e3-949a-0800200c9a66"),
            UUID.fromString("51f88041-5ea2-11e3-949a-0800200c9a66"),
            UUID.fromString("51f88042-5ea2-11e3-949a-0800200c9a66"),
            UUID.fromString("51f88043-5ea2-11e3-949a-0800200c9a66"),
            UUID.fromString("51f88044-5ea2-11e3-949a-0800200c9a66"),
            UUID.fromString("51f88045-5ea2-11e3-949a-0800200c9a66"),
            UUID.fromString("51f88046-5ea2-11e3-949a-0800200c9a66")
    };

    /**
     * Listeners interested in received messages from the connected bluetooth device(s)
     * implements this interface.
     * @since 1.2.0
     */
    public interface RelayListener {
        public void onMessageToRelay(MAVLinkPacket[] relayedPackets);
    }

    private static final Parser parser = new Parser();

    /**
     * Tracks whether or not the bluetooth server was started.
     */
    private final AtomicBoolean mIsStarted = new AtomicBoolean(false);

    /**
     * Handles the various threads used to implement the server's logic.
     */
    private ExecutorService mThreadHandler;

    /**
     * This thread handles connection with the gcs clients.
     */
    private AcceptThread mAcceptThread;

    /**
     * Routes message from the connected drone to the listening gcs clients.
     */
    private final DroneToGCSClientsRouter mDroneToGcs;

    /**
     * Routes message from the gcs clients to the drone.
     */
    private final GCSClientsToDroneRouter mGcsToDrone;

    /**
     * This is the bluetooth adapter.
     *
     * @since 1.2.0
     */
    private final BluetoothAdapter mAdapter;

    /**
     * Pool of uuids. The AcceptThread thread creates server sockets from this pool until it's
     * empty. At which point, it will block waiting for ConnectedThread to relinquish their uuid
     * (s).
     *
     * @since 1.2.0
     */
    private final LinkedBlockingQueue<UUID> mUUIDPool;

    /**
     * Contains a mapping from uuid to ConnectedThread
     *
     * @since 1.2.0
     */
    private final Map<UUID, ConnectedThread> mUuidToClient;

    public BluetoothServer() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter == null)
            throw new IllegalStateException("Bluetooth is not supported on this device.");

        //Add 7 randomly-generated UUIDS. These must match on both server and client.
        mUUIDPool = new LinkedBlockingQueue<UUID>(7);
        mUuidToClient = new ConcurrentHashMap<UUID, ConnectedThread>(7);

        for (final UUID uuid : UUIDS) {
            mUUIDPool.add(uuid);
        }

        /*
        Build the set of threads handling the logic
         */
        mDroneToGcs = new DroneToGCSClientsRouter(mUuidToClient.values(), "BT Drone 2 GCS Router");
        mGcsToDrone = new GCSClientsToDroneRouter("BT GCS 2 drone router");
    }

    /**
     * Adds the passed relay listener to the listeners set.
     * @param listener {@link BluetoothServer.RelayListener} object
     */
    public void addRelayListener(RelayListener listener){
        mGcsToDrone.addRelayListener(listener);
    }

    /**
     * Removes the passed relay listener from the listeners set.
     * @param listener {@link BluetoothServer.RelayListener} object
     */
    public void removeRelayListener(RelayListener listener){
        mGcsToDrone.removeListener(listener);
    }

    /**
     * Starts the bluetooth server.
     */
    public synchronized void start() {
        if (mIsStarted.compareAndSet(false, true)) {
            Log.d(TAG, "Starting bluetooth server.");

            mAcceptThread = new AcceptThread();

            mThreadHandler = Executors.newCachedThreadPool();
            mThreadHandler.execute(mDroneToGcs);
            mThreadHandler.execute(mGcsToDrone);
            mThreadHandler.execute(mAcceptThread);
        } else {
            Log.d(TAG, "Bluetooth server was already started.");
        }
    }

    /**
     * Stops the bluetooth server.
     */
    public synchronized void stop() {
        if (mIsStarted.compareAndSet(true, false)) {
            Log.d(TAG, "Stopping bluetooth server.");

            //Stop the accept thread
            if (mAcceptThread != null)
                mAcceptThread.cancel();

            //Stop the connected thread
            for (ConnectedThread gcsThread : mUuidToClient.values()) {
                gcsThread.cancel();
            }

            mThreadHandler.shutdownNow();
            mThreadHandler = null;
        } else {
            Log.d(TAG, "Bluetooth server was already stopped.");
        }
    }

    /**
     * Pass the mavlink packet to the drone to gcs clients routing thread.
     * @param packet MAVLink packet to be relayed
     */
    public void relayMavPacket(MAVLinkPacket packet){
        mDroneToGcs.addMsg(packet);
    }

    /**
     * Worker thread in charge of listening, and accepting incoming client connections.
     *
     * @since 1.2.0
     */
    private class AcceptThread implements Runnable {

        /**
         * Server socket currently listening for incoming connections.
         *
         * @since 1.2.0
         */
        private BluetoothServerSocket mServerSocket;

        /**
         * Keeps track of whether the thread is running or not.
         */
        private final AtomicBoolean mIsRunning = new AtomicBoolean(false);

        /**
         * Starts listening for incoming client connections.
         *
         * @since 1.2.0
         */
        public void run() {
            mIsRunning.set(true);
            Log.d(TAG, "Starting accept thread.");

            try {
                while (!Thread.currentThread().isInterrupted() && mIsRunning.get()) {
                    final UUID uuid = mUUIDPool.take();

                    mServerSocket = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, uuid);
                    BluetoothSocket socket = mServerSocket.accept();

                    //Close the server socket now the connection has been made
                    mServerSocket.close();
                    mServerSocket = null;

                    //Launch the connected thread.
                    ConnectedThread connectedThread = new ConnectedThread(uuid, socket);
                    mThreadHandler.execute(connectedThread);

                    //Update the uuid to client map.
                    mUuidToClient.put(uuid, connectedThread);
                }
            } catch (IOException e) {
                Log.e(TAG, "Accept failed", e);
            } catch (InterruptedException e) {
                Log.e(TAG, "UUID retrieval failed.", e);
            } finally {
                cancel();
            }

            Log.d(TAG, "Ending accept thread.");
        }

        /**
         * Stops the accept thread.
         *
         * @since 1.2.0
         */
        public void cancel() {
            if (mIsRunning.compareAndSet(true, false)){
                try {
                    Log.d(TAG, "Cancelling the accept thread.");
                    mServerSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Close of server socket failed.", e);
                }
            }
        }
    }

    /**
     * Worker thread in charge of handling communication with connected clients.
     *
     * @since 1.2.0
     */
    public class ConnectedThread implements Runnable {

        /**
         * Uuid the bluetooth socket is connected to.
         *
         * @since 1.2.0
         */
        private final UUID mUuid;

        /**
         * This is the bluetooth socket for the connected client this thread is communicating with.
         *
         * @since 1.2.0
         */
        private final BluetoothSocket mSocket;

        /**
         * InputStream to receive client's data.
         *
         * @since 1.2.0
         */
        private final InputStream mInStream;

        /**
         * OutputStream to send data to the client.
         *
         * @since 1.2.0
         */
        private final OutputStream mOutStream;

        private final AtomicBoolean mIsRunning = new AtomicBoolean(false);

        public ConnectedThread(UUID uuid, BluetoothSocket socket) {
            mUuid = uuid;
            mSocket = socket;

            try {
                mInStream = socket.getInputStream();
                mOutStream = socket.getOutputStream();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        /**
         * Reads incoming data from the connected client.
         *
         * @since 1.2.0
         */
        public void run() {
            mIsRunning.set(true);

            final String address = mSocket.getRemoteDevice().getAddress();
            Log.d(TAG, "Starting connected thread for " + address);

            byte[] buffer = new byte[4096];
            int bytes;

            //Keep listening to the input stream while connected
            try {
                while (!Thread.currentThread().isInterrupted() && mIsRunning.get()) {
                    //Read from the input stream
                    bytes = mInStream.read(buffer);

                    //Relayed the received messages
                    MAVLinkPacket[] receivedPackets = MAVLinkConnection.parseMavlinkBuffer
                            (parser, buffer, bytes);

                    //Relay the message from the gcs client to the drone
                    mGcsToDrone.addMsg(receivedPackets);
                }
            } catch (IOException e) {
                Log.e(TAG, "Disconnected", e);
            } finally {
                cancel();
            }
        }

        /**
         * Write to the connected output stream.
         *
         * @param buffer the bytes to write
         * @since 1.2.0
         */
        public void write(byte[] buffer) {
            try {
                mOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception occurred during write", e);
            }
        }

        /**
         * Stops the connected thread.
         *
         * @since 1.2.0
         */
        public void cancel() {
            if (mIsRunning.compareAndSet(true, false)) {
                Log.d(TAG, "Shutting down connected thread for uuid " + mUuid);

                try {
                    mSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "close() of connect socket failed.", e);
                }

                //Remove the mapping for this uuid.
                mUuidToClient.remove(mUuid);

                //Return the uuid to the pool.
                mUUIDPool.add(mUuid);
            }
        }
    }
}
