package com.droidplanner.MAVLink;

import com.google.android.gms.maps.model.LatLng;

public class DroneGPS extends DroneVariable {
	public double gps_eph;
	public int satCount;
	public int fixType;
	public LatLng position;

	public DroneGPS(Drone myDrone, double gps_eph, int satCount, int fixType) {
		super(myDrone);
		this.gps_eph = gps_eph;
		this.satCount = satCount;
		this.fixType = fixType;
	}

	public LatLng getPosition() {
		return position;
	}

	public double getGpsEPH() {
		return gps_eph;
	}

	public int getSatCount() {
		return satCount;
	}

	public int getFixType() {
		return fixType;
	}

	public void setGpsState(int fix, int satellites_visible, int eph) {
		if (satCount != satellites_visible | fixType != fix) {
			if (fixType != fix) {
				myDrone.tts.speakGpsMode(fix);
			}
			fixType = fix;
			satCount = satellites_visible;
			gps_eph = (double) eph / 100; //convert from eph(cm) to gps_eph(m)
			myDrone.notifyHudUpdate();
		}
	}

	public void setPosition(LatLng position) {
		if (this.position != position) {
			this.position = position;
			myDrone.notifyPositionChange();
		}
	}
}