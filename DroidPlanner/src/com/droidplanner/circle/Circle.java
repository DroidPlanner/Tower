package com.droidplanner.circle;

import com.droidplanner.drone.variables.Mission;
import com.droidplanner.fragments.PlanningMapFragment;
import com.droidplanner.polygon.GeoTools;
import com.google.android.gms.maps.model.LatLng;

public class Circle {
	public CirclePoint circleCenter;

	public Circle() {
	}

	public void generateCircle(Mission mission, int numberOfWaypoints) {
		for (int i = 0; i < numberOfWaypoints; i++) {
			double heading = (360.0*i)/numberOfWaypoints;
			mission.addWaypoint(GeoTools.newpos(circleCenter.coord, heading, 100.0));			
		}
	}

	public void moveCircle(PlanningMapFragment planningMapFragment, LatLng point) {
		if (circleCenter == null) {
			circleCenter = new CirclePoint(point);
		}else{
			circleCenter.coord = point;
		}
		planningMapFragment.updateCircle(circleCenter);
		return;
	}
}