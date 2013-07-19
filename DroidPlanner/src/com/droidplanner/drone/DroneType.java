package com.droidplanner.drone;

import com.MAVLink.Messages.enums.MAV_TYPE;

public class DroneType extends DroneVariable{
	private int type = MAV_TYPE.MAV_TYPE_FIXED_WING;
	
	public DroneType(Drone myDrone) {
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
}