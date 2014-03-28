package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneVariable;

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

}
