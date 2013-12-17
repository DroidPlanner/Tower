package com.droidplanner.drone.variables;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_vfr_hud;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

public class Speed extends DroneVariable {
	private double verticalSpeed = 0;
	private double groundSpeed = 0;
	private double airSpeed = 0;
	private double targetSpeed = 0;
	private double altitude = 0;

	public Speed(Drone myDrone) {
		super(myDrone);
	}

	public double getVerticalSpeed() {
		return verticalSpeed;
	}

	public double getGroundSpeed() {
		return groundSpeed;
	}

	public double getAirSpeed() {
		return airSpeed;
	}

	public double getTargetSpeed() {
		return targetSpeed;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setSpeedError(double aspd_error) {
		targetSpeed = aspd_error + airSpeed;
	}

	public void setHUDState(double altitude, double groundSpeed,
			double airSpeed, double climb) {
		if (altitude != this.altitude | groundSpeed != this.groundSpeed
				| airSpeed != this.airSpeed | climb != this.verticalSpeed) {
			this.groundSpeed = groundSpeed;
			this.airSpeed = airSpeed;
			this.verticalSpeed = climb;
			myDrone.notifyHUDChange();
		}
	}

	@Override
	protected void processMAVLinkMessage(MAVLinkMessage msg) {
		switch (msg.msgid) {
		case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
			msg_vfr_hud m_hud = (msg_vfr_hud) msg;
			setHUDState(m_hud.alt, m_hud.groundspeed, m_hud.airspeed,
					m_hud.climb);
		}
	}
}