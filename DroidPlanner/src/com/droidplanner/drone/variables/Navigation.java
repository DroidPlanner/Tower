package com.droidplanner.drone.variables;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_nav_controller_output;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

public class Navigation extends DroneVariable {

	private double nav_pitch;
	private double nav_roll;
	private double nav_bearing;
	private double nav_wp_dist;
	private double nav_alt_error;
	private double nav_aspd_error;

	public Navigation(Drone myDrone) {
		super(myDrone);
	}

	public double getNavPitch() {
		return nav_pitch;
	}

	public double getNavRoll() {
		return nav_roll;
	}

	public double getNavBearing() {
		return nav_bearing;
	}

	public double getNavWpDist() {
		return nav_wp_dist;
	}

	public double getNavAltError() {
		return nav_alt_error;
	}

	public double getNavAspdError() {
		return nav_aspd_error;
	}

	private void setNavState(short wp_dist, float alt_error, float aspd_error,
			float nav_pitch, float nav_roll, short nav_bearing) {
		this.nav_pitch = (double) nav_pitch;
		this.nav_roll = (double) nav_roll;
		this.nav_bearing = (double) nav_bearing;
		this.nav_alt_error = (double) alt_error;
		this.nav_aspd_error = (double) aspd_error;
		this.nav_wp_dist = (double) wp_dist;
		myDrone.notifyNavDataChange();
	}

	@Override
	protected void processMAVLinkMessage(MAVLinkMessage msg) {
		switch (msg.msgid) {
		case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
			msg_nav_controller_output m_nav = (msg_nav_controller_output) msg;
			setNavState(m_nav.wp_dist, m_nav.alt_error, m_nav.aspd_error,
					m_nav.nav_pitch, m_nav.nav_roll, m_nav.nav_bearing);
			break;
		}
	}

}
