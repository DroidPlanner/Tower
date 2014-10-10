package org.droidplanner.core.drone.variables.helpers;

import java.util.ArrayList;

import org.droidplanner.core.MAVLink.MavLinkStreamRates;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;

import ellipsoidFit.FitPoints;
import ellipsoidFit.ThreeSpacePoint;

public class MagnetometerCalibration implements OnDroneListener {
	FitPoints ellipsoidFit = new FitPoints();
	public ArrayList<ThreeSpacePoint> points = new ArrayList<ThreeSpacePoint>();
	private boolean fitComplete =false;
	private OnMagCalibrationListner listner;
	private Drone drone;

	public MagnetometerCalibration(Drone drone, OnMagCalibrationListner listner) {
		this.drone = drone;
		drone.addDroneListener(this);
		this.listner = listner;
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
		if (listner!=null) {
			listner.newEstimation(ellipsoidFit,points.size(),magVector);
		}
		
		
		if (!fitComplete && ellipsoidFit.getFitness() > 0.98 && points.size() > 100) {
			fitComplete  = true;
			if (listner!=null) {
				listner.finished(ellipsoidFit);
			}
		}
	}

	public void sendOffsets() {
		Parameter offsetX = drone.getParameters().getParameter("COMPASS_OFS_X");
		Parameter offsetY = drone.getParameters().getParameter("COMPASS_OFS_Y");
		Parameter offsetZ = drone.getParameters().getParameter("COMPASS_OFS_Z");
		
		offsetX.value = ellipsoidFit.center.getEntry(0);
		offsetY.value = ellipsoidFit.center.getEntry(1);
		offsetZ.value = ellipsoidFit.center.getEntry(2);
		
		drone.getParameters().sendParameter(offsetX); //TODO should probably do a check after sending the parameters
		drone.getParameters().sendParameter(offsetY);
		drone.getParameters().sendParameter(offsetZ);
	}

	public interface OnMagCalibrationListner {
		public void newEstimation(FitPoints fit, int sampleSize, int[] magVector);
		public void finished(FitPoints fit);
	}
}