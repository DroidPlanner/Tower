package com.droidplanner.circle;

import com.droidplanner.drone.variables.Mission;
import com.droidplanner.fragments.PlanningMapFragment;
import com.droidplanner.polygon.GeoTools;
import com.google.android.gms.maps.model.LatLng;

public class Circle {
	public CirclePoint circleCenter;
	public double radius = 100.0;
	int numberOfWaypoints = 10;
	
	public Circle() {
	}

	public void generateCircle(Mission mission) {
		for (int i = 0; i < numberOfWaypoints; i++) {
			double heading = (360.0*i)/numberOfWaypoints;
			mission.addWaypoint(GeoTools.newpos(circleCenter.coord, heading, radius));			
		}
	}

	public void moveCircle(PlanningMapFragment planningMapFragment, LatLng point) {
		if (circleCenter == null) {
			circleCenter = new CirclePoint(point);
		}else{
			circleCenter.coord = point;
		}
		planningMapFragment.updateCircle(this);
		return;
	}
}