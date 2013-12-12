package com.droidplanner.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.MAVLink.Messages.MAVLinkPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

    public interface RelayListener {
        public void onReceivedRelayedMessage(MAVLinkPacket packet);
    }

    /**
     * This is the bluetooth adapter.
     *
     * @since 1.2.0
     */
    private final BluetoothAdapter mAdapter;

    /**
     * Handle to the worker thread.
     *
     * @since 1.2.0
     */
    private AcceptThread mAcceptThread;

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
    }

    /**
     * Starts the bluetooth server.
     *
     * @since 1.2.0
     */
    public synchronized void start() {
        Log.d(TAG, "Starting bluetooth server.");
        //Start the worker thread.
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    /**
     * Stops the bluetooth server.
     *
     * @since 1.2.0
     */
    public synchronized void stop() {
        Log.d(TAG, "Stopping bluetooth server.");
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        for (final ConnectedThread connectedThread : mUuidToClient.values()) {
            if (connectedThread != null)
                connectedThread.cancel();
        }
    }

    /**
     * Format and relay a Mavlink packet via bluetooth
     * @param packet MAVLink packet to be relayed
     */
    public void relayMavPacket(MAVLinkPacket packet){
        byte[] buffer = packet.encodePacket();

        //Write to all the connected thread.
         for(ConnectedThread connectedThread: mUuidToClient.values()){
             if(connectedThread != null)
                 connectedThread.write(buffer);
         }
    }

    /**
     * Worker thread in charge of listening, and accepting incoming client connections.
     *
     * @since 1.2.0
     */
    private class AcceptThread extends Thread {

        /**
         * Server socket currently listening for incoming connections.
         *
         * @since 1.2.0
         */
        private BluetoothServerSocket mServerSocket;

        /**
         * Whether or not the thread should be running.
         *
         * @since 1.2.0
         */
        private final AtomicBoolean mIsRunning = new AtomicBoolean(true);

        /**
         * Starts listening for incoming client connections.
         *
         * @since 1.2.0
         */
        public void run() {
            Log.d(TAG, "Starting accept thread.");
            setName(AcceptThread.class.getSimpleName());

            try {
                while (mIsRunning.get()) {
                    final UUID uuid = mUUIDPool.take();

                    mServerSocket = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, uuid);
                    BluetoothSocket socket = mServerSocket.accept();

                    //Close the server socket now the connection has been made
                    mServerSocket.close();
                    mServerSocket = null;

                    //Launch the connected thread.
                    ConnectedThread connectedThread = new ConnectedThread(uuid, socket);
                    connectedThread.start();

                    //Update the uuid to client map.
                    mUuidToClient.put(uuid, connectedThread);
                }
            } catch (IOException e) {
                Log.e(TAG, "Accept failed", e);
            } catch (InterruptedException e) {
                Log.e(TAG, "UUID retrieval failed.", e);
            }

            Log.d(TAG, "Ending accept thread.");
        }

        /**
         * Stops the accept thread.
         *
         * @since 1.2.0
         */
        public void cancel() {
            Log.d(TAG, "Cancelling the accept thread.");
            mIsRunning.set(false);

            if (mServerSocket != null)
                try {
                    mServerSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Close of server socket failed.", e);
                }
        }
    }

    /**
     * Worker thread in charge of handling communication with connected clients.
     *
     * @since 1.2.0
     */
    private class ConnectedThread extends Thread {

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

        /**
         * Wether or not the thread should run.
         *
         * @since 1.2.0
         */
        private final AtomicBoolean mIsRunning;

        public ConnectedThread(UUID uuid, BluetoothSocket socket) {
            mUuid = uuid;
            mSocket = socket;

            try {
                mInStream = socket.getInputStream();
                mOutStream = socket.getOutputStream();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

            mIsRunning = new AtomicBoolean(true);
        }

        /**
         * Reads incoming data from the connected client.
         *
         * @since 1.2.0
         */
        public void run() {
            final String address = mSocket.getRemoteDevice().getAddress();
            Log.d(TAG, "Starting connected thread for " + address);

            byte[] buffer = new byte[1024];
            int bytes;

            //Keep listening to the input stream while connected
            while (mIsRunning.get()) {
                try {
                    //Read from the input stream
                    bytes = mInStream.read(buffer);

                    //TODO: figure out what to do with the incoming data
                } catch (IOException e) {
                    Log.e(TAG, "Disconnected", e);
                    cancel();
                    break;
                }
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
            mIsRunning.set(false);

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
