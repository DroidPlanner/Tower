package org.droidplanner.core.drone.variables.helpers;

import java.util.ArrayList;
import java.util.Arrays;

import org.droidplanner.core.MAVLink.MavLinkStreamRates;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;

import ellipsoidFit.FitPoints;
import ellipsoidFit.ThreeSpacePoint;

public class MagnetometerCalibration implements OnDroneListener {
	FitPoints ellipsoidFit = new FitPoints();
	public ArrayList<ThreeSpacePoint> points = new ArrayList<ThreeSpacePoint>();

	public MagnetometerCalibration(Drone drone) {
		drone.addDroneListener(this);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MAGNETOMETER:
			int[] magVector = addpoint(drone);
			fit(magVector);
			MavLinkStreamRates.setupStreamRates(drone.getMavClient(), 0, 0, 0, 0, 0, 0, 50, 0);
			break;
		default:
			break;
		}
	}

	int[] addpoint(Drone drone) {
		int[] magVector = drone.getMagnetometer().getVector();
		ThreeSpacePoint point = new ThreeSpacePoint(magVector[0], magVector[1], magVector[2]);
		points.add(point);
		return magVector;
	}

	void fit(int[] magVector) {
		ellipsoidFit.fitEllipsoid(points);
		System.out.println("IMU" + Arrays.toString(magVector) + " \t" + points.size() + "\tCenter:"
				+ ellipsoidFit.center.toString() + "\tRadii:" + ellipsoidFit.radii.toString()
				+ "\t\t Eigenvector" + ellipsoidFit.evecs);
	}
}