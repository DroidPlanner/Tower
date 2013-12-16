package com.droidplanner.drone;

import com.MAVLink.Messages.MAVLinkMessage;
import com.droidplanner.MAVLink.MavLinkMsgHandler.OnMavLinkMsgListener;

public class DroneVariable implements OnMavLinkMsgListener{
	protected Drone myDrone;

	public DroneVariable(Drone myDrone) {
		this.myDrone = myDrone;
	}

	@Override
	public void onMavLinkMsg(MAVLinkMessage msg) {
		processMAVLinkMessage(msg);
	}

	protected void processMAVLinkMessage(MAVLinkMessage msg) {
		// TODO Auto-generated method stub
	}
}