package com.droidplanner.MAVLink;

import com.google.android.gms.maps.model.LatLng;

public class DroneGPS {
	public double gps_eph;
	public int satCount;
	public int fixType;
	public LatLng position;

	public DroneGPS(double gps_eph, int satCount, int fixType) {
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
}