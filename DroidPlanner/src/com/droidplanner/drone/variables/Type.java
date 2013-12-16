package com.droidplanner.drone.variables;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

public class Type extends DroneVariable {
	private int type = MAV_TYPE.MAV_TYPE_FIXED_WING;

	public Type(Drone myDrone) {
		super(myDrone);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		if (this.type != type) {
			this.type = type;
			myDrone.notifyTypeChanged();
		}
	}
	
	@Override
	protected void processMAVLinkMessage(MAVLinkMessage msg) {
		switch (msg.msgid) {
		case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
			msg_heartbeat msg_heart = (msg_heartbeat) msg;
			setType(msg_heart.type);
			break;
		}
	}

}