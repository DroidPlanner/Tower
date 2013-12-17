package com.droidplanner.drone.variables;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_mission_current;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

public class MissionStats extends DroneVariable {
	private double distanceToWp = 0;
	private short goingForWaypoint = -1;

	public MissionStats(Drone myDrone) {
		super(myDrone);
	}

	public void setDistanceToWp(double disttowp) {
		this.distanceToWp = disttowp;
	}

	public void setWpno(short seq) {
		goingForWaypoint = seq;

	}

	@Override
	protected void processMAVLinkMessage(MAVLinkMessage msg) {
		switch (msg.msgid) {
		case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
			setWpno(((msg_mission_current) msg).seq);
			break;
		}
	}
}
