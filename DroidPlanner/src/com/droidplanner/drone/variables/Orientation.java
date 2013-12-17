package com.droidplanner.drone.variables;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_attitude;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

public class Orientation extends DroneVariable {
	private double roll = 0;
	private double pitch = 0;
	private double yaw = 0;

	public Orientation(Drone myDrone) {
		super(myDrone);
	}

	public double getRoll() {
		return roll;
	}

	public double getPitch() {
		return pitch;
	}

	public double getYaw() {
		return yaw;
	}

	public void setRollPitchYaw(double roll, double pitch, double yaw) {
		this.roll = roll;
		this.pitch = pitch;
		this.yaw = yaw;
		myDrone.notifyOrientationChange();
		myDrone.notifyNavDataChange();
	}

	@Override
	protected void processMAVLinkMessage(MAVLinkMessage msg) {
		switch (msg.msgid) {
		case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
			msg_attitude m_att = (msg_attitude) msg;
			setRollPitchYaw(m_att.roll * 180.0 / Math.PI,
					m_att.pitch * 180.0 / Math.PI, m_att.yaw * 180.0 / Math.PI);
			break;
		}
	}
}