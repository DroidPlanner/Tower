package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.*;
import org.droidplanner.core.model.Drone;

public class GPS extends DroneVariable {
	public final static int LOCK_2D = 2;
	public final static int LOCK_3D = 3;

	private Drone myDrone;
	private double gps_eph = -1;
	private int satCount = -1;
	private int fixType = -1;
	private Coord2D position;
	private long timeOfPosition = System.currentTimeMillis();
	private double course = 0;

	public GPS(Drone myDrone) {
		super(myDrone);
		this.myDrone = myDrone;
	}

	public boolean isPositionValid() {
		return (position != null);
	}

	public Coord2D getPosition() {
		return position;
	}

	public double getGpsEPH() {
		return gps_eph;
	}

	public int getSatCount() {
		return satCount;
	}

	public String getFixType() {
		String gpsFix = "";
		switch (fixType) {
		case LOCK_2D:
			gpsFix = ("2D");
			break;
		case LOCK_3D:
			gpsFix = ("3D");
			break;
		default:
			gpsFix = ("NoFix");
			break;
		}
		return gpsFix;
	}

	public int getFixTypeNumeric() {
		return fixType;
	}

	public double getCourse(){
		return course;
	}

	public int getPositionAgeInMillis(){
		return (int) (System.currentTimeMillis()-timeOfPosition);
	}

	public Coord2D getInterpolatedPosition(){
		Coord2D realPosition = getPosition();
		if(myDrone.getMavClient().isConnected()){
			int timeDelta = myDrone.getGps().getPositionAgeInMillis();
			org.droidplanner.core.helpers.units.Speed groundSpeed = myDrone.getSpeed()
					.getGroundSpeed();
			double course = myDrone.getGps().getCourse();
			return GeoTools.newCoordFromBearingAndDistance(realPosition,course,
					timeDelta/1000.0* groundSpeed.valueInMetersPerSecond());
		}else{
			return realPosition;
		}
	}

	public void setGpsState(int fix, int satellites_visible, int eph) {
		if (satCount != satellites_visible) {
			satCount = satellites_visible;
			gps_eph = (double) eph / 100; // convert from eph(cm) to gps_eph(m)
			myDrone.notifyDroneEvent(DroneEventsType.GPS_COUNT);
		}
		if (fixType != fix) {
			fixType = fix;
			myDrone.notifyDroneEvent(DroneEventsType.GPS_FIX);
		}
	}

	public void setPosition(Coord2D position) {
		this.timeOfPosition = System.currentTimeMillis();
		recalculateCourse(position);
		if (this.position != position) {
			this.position = position;
			myDrone.notifyDroneEvent(DroneEventsType.GPS);
		}
	}

	private void recalculateCourse(Coord2D position) {
		course = GeoTools.getHeadingFromCoordinates(this.position, position);
	}
}
