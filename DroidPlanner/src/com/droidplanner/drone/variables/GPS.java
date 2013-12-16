package com.droidplanner.drone.variables;

import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.google.android.gms.internal.fi;
import com.google.android.gms.maps.model.LatLng;

public class GPS extends DroneVariable {
	private double gps_eph = -1;
	private int satCount = -1;
	private int fixType = -1;
	private LatLng position;

	public GPS(Drone myDrone) {
		super(myDrone);
	}

	public boolean isPositionValid() {
		return (position != null);
	}

	public LatLng getPosition() {
		if (isPositionValid()) {
			return position;			
		}else{
			return new LatLng(0, 0);
		}
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
		case 2:
			gpsFix = ("2D");
			break;
		case 3:
			gpsFix = ("3D");
			break;
		default:
			gpsFix = ("NoFix");
			break;
		}
		return gpsFix;
	}
	public int getFixTypeNumeric(){
		return fixType;
	}

	public void setGpsState(int fix, int satellites_visible, int eph) {
		if(satCount != satellites_visible){
			satCount = satellites_visible;			
			gps_eph = (double) eph / 100; // convert from eph(cm) to gps_eph(m)
			myDrone.events.notifyDroneEvent(DroneEventsType.GPS_COUNT);
		}
		if (fixType != fix) {
			fixType = fix;
			myDrone.events.notifyDroneEvent(DroneEventsType.GPS_FIX);
		}
	}

	public void setPosition(LatLng position) {
		if (this.position != position) {
			this.position = position;
			myDrone.events.notifyDroneEvent(DroneEventsType.GPS);
		}
	}
}