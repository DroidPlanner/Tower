package com.droidplanner.gcs;

import android.util.Log;
import com.droidplanner.MAVLink.MavLinkHeartbeat;
import com.droidplanner.drone.Drone;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to send periodic heartbeat messages to the drone.
 * @author Fredia Huya-Kouadio
 * @since 1.2.0
 */
public class GCSHeartbeat {

    /**
     * Used for logging.
     * @since 1.2.0
     */
    private static final String TAG = GCSHeartbeat.class.getName();

    /**
     * This is the drone to send the heartbeat message to.
     * @since 1.2.0
     */
    private final Drone drone;

    /**
     * This specifies whether or not the heartbeat should be activate.
     * @since 1.2.0
     */
    private boolean active;

    /**
     * This is the heartbeat period in seconds.
     * @since 1.2.0
     */
    private final int freqHz;

    /**
     * ScheduledExecutorService used to periodically schedule the heartbeat.
     * @since 1.2.0
     */
    private ScheduledExecutorService heartbeatExecutor;

    /**
     * This heartbeatRunnable is run periodically to generate the heartbeat message.
     * @since 1.2.0
     */
    private final Runnable heartbeatRunnable = new Runnable() {
        @Override
        public void run() {
            MavLinkHeartbeat.sendMavHeartbeat(drone);
            Log.d(TAG, "beating");
        }
    };

    public GCSHeartbeat(Drone drone, int freqHz){
        this.drone = drone;
        this.freqHz = freqHz;
    }

    /**
     * @return true if the heartbeat is active.
     * @since 1.2.0
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set the state of the heartbeat.
     * @param active true to activate the heartbeat, false to deactivate it
     * @since 1.2.0
     */
    public void setActive(boolean active) {
        this.active = active;
        if(active){
            heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
            heartbeatExecutor.scheduleWithFixedDelay(heartbeatRunnable, 0, freqHz, TimeUnit.SECONDS);
        }
        else if(heartbeatExecutor != null){
            heartbeatExecutor.shutdownNow();
            heartbeatExecutor = null;
        }
    }
}
