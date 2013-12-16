package com.droidplanner.MAVLink;

import android.os.Handler;
import android.util.Log;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.enums.MAV_AUTOPILOT;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.droidplanner.drone.Drone;

public class MavLinkHeartbeat {

    /**
     * This is the msg heartbeat used to check the drone is present, and responding.
     * @since 1.2.0
     */
    private static final msg_heartbeat sMsg = new msg_heartbeat();
    static {
        sMsg.type = MAV_TYPE.MAV_TYPE_GCS;
        sMsg.autopilot = MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC;
    }

    /**
     * This is the mavlink packet obtained from the msg heartbeat,
     * and used for actual communication.
     * @since 1.2.0
     */
    private static final MAVLinkPacket sMsgPacket = sMsg.pack();

	private final Drone drone;
	private boolean active;
	private final int freqHz;
	private final Handler handler = new Handler();
	
	public MavLinkHeartbeat(Drone drone,int freqHz){
		this.drone = drone;
		this.freqHz = freqHz;
	}

	private final Runnable runnable = new Runnable() {
	   @Override
	   public void run() {
	      sendMavHeartbeat(drone);
		  Log.d("Heartbeat", "beating");
		  
		   if(isActive())
			  handler.postDelayed(this, freqHz*1000);
	   }
	};
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
		if(active){
			handler.postDelayed(runnable, freqHz*1000);			
		}
	}

	public static void sendMavHeartbeat(Drone drone) {
		if(drone!=null)
		drone.MavClient.sendMavPacket(sMsgPacket);
	}

}
