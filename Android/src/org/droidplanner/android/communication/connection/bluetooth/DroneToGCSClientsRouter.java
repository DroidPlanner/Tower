package org.droidplanner.android.communication.connection.bluetooth;

import android.util.Log;

import com.MAVLink.Messages.MAVLinkPacket;

import org.droidplanner.android.communication.connection.bluetooth.BluetoothServer.ConnectedThread;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles routing communication from the connected drone, and the listening GCS clients.
 */
public class DroneToGCSClientsRouter implements Runnable {

    /**
     * Identifier for this runnable.
     */
    private final String mTag;

    /**
     * Set of connected GCS clients.
     */
    private final Collection<ConnectedThread> mGCSClients;

    /**
     * Concurrent message queue. The thread will block on it until elements are added.
     */
    private final LinkedBlockingQueue<MAVLinkPacket> mMsgQueue;

    public DroneToGCSClientsRouter(Collection<ConnectedThread> gcsClients, String tag) {
        mTag = tag;
        mGCSClients = gcsClients;
        mMsgQueue = new LinkedBlockingQueue<MAVLinkPacket>();
    }

    /**
     * Add a mavlink packet msg to the message queue. If the queue is not full,
     * the message will be relayed to all connected GCS clients.
     *
     * @param packet MAVLinkPacket to be relayed
     * @return true if the message was successfully added, false otherwise.
     */
    public boolean addMsg(MAVLinkPacket packet) {
        return mMsgQueue.offer(packet);
    }

    @Override
    public void run() {
        try {
            while (true) {
                final MAVLinkPacket packet = mMsgQueue.take();
                byte[] buffer = packet.encodePacket();

                //Write to all the connected GCS clients
                for (ConnectedThread connectedGCS : mGCSClients) {
                    if (connectedGCS != null) {
                        connectedGCS.write(buffer);
                    }
                }
            }
        } catch (InterruptedException e) {
            Log.e(mTag, "Error occurred while retrieving mavlink packet message.", e);
        }

        Log.d(mTag, "Ending relay thread.");
    }
}
