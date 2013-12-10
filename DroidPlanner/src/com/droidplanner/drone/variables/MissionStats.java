package com.droidplanner.drone.variables;

import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

public class MissionStats extends DroneVariable{
	private double distanceToWp = 0;
	private short goingForWaypoint = -1;

	public MissionStats(Drone myDrone) {
		super(myDrone);
	}

	public void setDistanceToWp(double disttowp) {
		this.distanceToWp  = disttowp;
	}

	public void setWpno(short seq) {
		goingForWaypoint  = seq;
		
	}

}
