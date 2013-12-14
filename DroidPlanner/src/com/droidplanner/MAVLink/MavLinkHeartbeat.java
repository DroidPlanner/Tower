package com.droidplanner.MAVLink;

import android.os.Handler;
import android.util.Log;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.enums.MAV_AUTOPILOT;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.droidplanner.drone.Drone;

public class MavLinkHeartbeat {
	private Drone drone;
	private boolean active;
	private int freqHz;
	private Handler handler = new Handler();
	
	public MavLinkHeartbeat(Drone drone,int freqHz){
		this.drone = drone;
		this.freqHz = freqHz;
	}

	private Runnable runnable = new Runnable() {
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
		msg_heartbeat msg = new msg_heartbeat();
		msg.type = MAV_TYPE.MAV_TYPE_GCS;
		msg.autopilot = MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC;	
		if(drone!=null)
		drone.MavClient.sendMavPacket(msg.pack());
	}

}
