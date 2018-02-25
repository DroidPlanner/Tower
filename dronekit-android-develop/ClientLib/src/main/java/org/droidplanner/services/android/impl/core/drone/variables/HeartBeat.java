package org.droidplanner.services.android.impl.core.drone.variables;

import android.os.Handler;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_heartbeat;

import org.droidplanner.services.android.impl.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.services.android.impl.core.drone.DroneVariable;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;

import timber.log.Timber;

public class HeartBeat extends DroneVariable implements OnDroneListener<MavLinkDrone> {

    public static final long HEARTBEAT_NORMAL_TIMEOUT = 5000l; //ms
    private static final long HEARTBEAT_LOST_TIMEOUT = 15000l; //ms

    public static final int INVALID_MAVLINK_VERSION = -1;

    protected static final int FIRST_HEARTBEAT = 0;
    protected static final int LOST_HEARTBEAT = 1;
    protected static final int NORMAL_HEARTBEAT = 2;

    protected int heartbeatState = FIRST_HEARTBEAT;
    private short sysid = 1;
    private short compid = 1;

    /**
     * Stores the version of the mavlink protocol.
     */
    private short mMavlinkVersion = INVALID_MAVLINK_VERSION;

    public final Handler watchdog;
    public final Runnable watchdogCallback = new Runnable() {
        @Override
        public void run() {
            onHeartbeatTimeout();
        }
    };

    public HeartBeat(MavLinkDrone myDrone, Handler handler) {
        super(myDrone);
        this.watchdog = handler;
        myDrone.addDroneListener(this);
    }

    public short getSysid() {
        return sysid;
    }

    public short getCompid() {
        return compid;
    }

    /**
     * @return the version of the mavlink protocol.
     */
    public short getMavlinkVersion() {
        return mMavlinkVersion;
    }

    public void onHeartbeat(MAVLinkMessage msg) {
        msg_heartbeat heartBeatMsg = msg instanceof msg_heartbeat ? (msg_heartbeat) msg : null;
        if(heartBeatMsg != null){
            sysid  = validateToUnsignedByteRange(msg.sysid);
            compid = validateToUnsignedByteRange(msg.compid);
            mMavlinkVersion = heartBeatMsg.mavlink_version;
        }

        switch (heartbeatState) {
            case FIRST_HEARTBEAT:
                if(heartBeatMsg != null) {
                    Timber.i("Received first heartbeat.");

                    heartbeatState = NORMAL_HEARTBEAT;
                    restartWatchdog(HEARTBEAT_NORMAL_TIMEOUT);

                    myDrone.notifyDroneEvent(DroneEventsType.HEARTBEAT_FIRST);
                }
                break;

            case LOST_HEARTBEAT:
                myDrone.notifyDroneEvent(DroneEventsType.HEARTBEAT_RESTORED);
            // FALL THROUGH

            default:
                heartbeatState = NORMAL_HEARTBEAT;
                restartWatchdog(HEARTBEAT_NORMAL_TIMEOUT);
                break;
        }
    }

    public boolean hasHeartbeat() {
        return heartbeatState != FIRST_HEARTBEAT;
    }

    public boolean isConnectionAlive() {
        return heartbeatState != LOST_HEARTBEAT;
    }

    @Override
    public void onDroneEvent(DroneEventsType event, MavLinkDrone drone) {
        switch (event) {
            case DISCONNECTED:
                notifyDisconnected();
                break;

            default:
                break;
        }
    }

    private void notifyDisconnected() {
        watchdog.removeCallbacks(watchdogCallback);
        heartbeatState = FIRST_HEARTBEAT;
        mMavlinkVersion = INVALID_MAVLINK_VERSION;
    }

    protected void onHeartbeatTimeout() {
        switch (heartbeatState) {
            case FIRST_HEARTBEAT:
                Timber.i("First heartbeat timeout.");
                myDrone.notifyDroneEvent(DroneEventsType.HEARTBEAT_TIMEOUT);
                break;

            default:
                heartbeatState = LOST_HEARTBEAT;
                restartWatchdog(HEARTBEAT_LOST_TIMEOUT);
                myDrone.notifyDroneEvent(DroneEventsType.HEARTBEAT_TIMEOUT);
                break;
        }
    }

    protected void restartWatchdog(long timeout) {
        // re-start watchdog
        watchdog.removeCallbacks(watchdogCallback);
        watchdog.postDelayed(watchdogCallback, timeout);
    }
}
