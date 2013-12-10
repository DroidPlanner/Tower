package com.droidplanner.drone.variables;

import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

public class Navigation extends DroneVariable {

	private double nav_pitch;
	private double nav_roll;
	private double nav_bearing;

	public Navigation(Drone myDrone) {
		super(myDrone);
	}

	public void setNavPitchRollYaw(float nav_pitch, float nav_roll,
			short nav_bearing) {
		this.nav_pitch = (double) nav_pitch;
		this.nav_roll = (double) nav_roll;
		this.nav_bearing = (double) nav_bearing;
		notifyNewNavigationData();
	}

	private void notifyNewNavigationData() {
		if (myDrone.tuningDataListner != null) {
			myDrone.tuningDataListner.onNewNavigationData();
		}
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

}
