package org.droidplanner.connection.bluetooth;

import android.util.Log;

import com.MAVLink.Messages.MAVLinkPacket;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import org.droidplanner.connection.bluetooth.BluetoothServer.RelayListener;

/**
 * Handles routing communication from the gcs clients to the drone.
 */
public class GCSClientsToDroneRouter implements Runnable {

    /**
     * Identifier for this runnable
     */
    private final String mTag;

    /**
     * Concurrent message queue. Messages in this queue are sent from the connected gcs clients
     * to the drone.
     */
    private final LinkedBlockingQueue<MAVLinkPacket[]> mMsgQueue = new
            LinkedBlockingQueue<MAVLinkPacket[]>();

    /**
     * Stores the set of relay listeners.
     */
    private final Map<RelayListener, Boolean> mRelayListenerSet = new
            ConcurrentHashMap<RelayListener, Boolean>();

    public GCSClientsToDroneRouter(String tag){
        mTag = tag;
    }

    /**
     * Adds the passed relay listener to the listeners set.
     * @param listener {@link BluetoothServer.RelayListener} object
     */
    public void addRelayListener(RelayListener listener){
        if(listener != null)
        mRelayListenerSet.put(listener, Boolean.TRUE);
    }

    /**
     * Removes the passed relay listener from the listeners set.
     * @param listener {@link BluetoothServer.RelayListener} object
     */
    public void removeListener(RelayListener listener){
        if(listener != null)
        mRelayListenerSet.remove(listener);
    }

    public boolean addMsg(MAVLinkPacket[] msg){
        return mMsgQueue.offer(msg);
    }

    @Override
    public void run() {
        try{
            while(true){
                final MAVLinkPacket[] packet = mMsgQueue.take();

                for(RelayListener listener: mRelayListenerSet.keySet()){
                    listener.onMessageToRelay(packet);
                }
            }
        }
        catch(InterruptedException e){
            Log.e(mTag, "Error occurred while retrieving mavlink packet message.", e);
        }

        Log.d(mTag, "Ending relay thread.");
    }
}
