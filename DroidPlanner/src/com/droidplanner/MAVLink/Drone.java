package com.droidplanner.MAVLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.droidplanner.helpers.TTS;
import com.google.android.gms.maps.model.LatLng;

public class Drone {
	public waypoint home;
	public Double defaultAlt;
	public List<waypoint> waypoints;

	private double roll = 0, pitch = 0, yaw = 0, altitude = 0, disttowp = 0,
			verticalSpeed = 0, groundSpeed = 0, airSpeed = 0, targetSpeed = 0,
			targetAltitude = 0, battVolt = -1, battRemain = -1,
			battCurrent = -1;
	private int wpno = -1, satCount = -1, fixType = -1,
			type = MAV_TYPE.MAV_TYPE_FIXED_WING;
	private boolean failsafe = false, armed = false;
	private ApmModes mode = ApmModes.UNKNOWN;
	private LatLng position;

	private HudUpdatedListner hudListner;
	private MapUpdatedListner mapListner;
	private DroneTypeListner typeListner;
	private TTS tts;
	

	public interface HudUpdatedListner {
		public void onDroneUpdate();
	}

	public interface MapUpdatedListner {
		public void onDroneUpdate();
	}

	public interface DroneTypeListner {
		public void onDroneTypeChanged();
	}

	public Drone(TTS tts) {
		super();
		this.tts = tts;
		this.home = new waypoint(0.0, 0.0, 0.0);
		this.defaultAlt = 50.0;
		this.waypoints = new ArrayList<waypoint>();
	}

	public void setHudListner(HudUpdatedListner listner) {
		hudListner = listner;
	}

	public void setMapListner(MapUpdatedListner listner) {
		mapListner = listner;
	}

	public void setDroneTypeChangedListner(DroneTypeListner listner) {
		typeListner = listner;
	}

	public void setRollPitchYaw(double roll, double pitch, double yaw) {
		this.roll = roll;
		this.pitch = pitch;
		this.yaw = yaw;
		notifyHudUpdate();
	}

	public void setAltitudeGroundAndAirSpeeds(double altitude,
			double groundSpeed, double airSpeed) {
		this.altitude = altitude;
		this.groundSpeed = groundSpeed;
		this.airSpeed = airSpeed;
		notifyHudUpdate();
	}

	public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error,
			double aspd_error) {
		this.disttowp = disttowp;
		targetAltitude = alt_error + altitude;
		targetSpeed = aspd_error + airSpeed;
		notifyHudUpdate();
	}

	public void setBatteryState(double battVolt, double battRemain,
			double battCurrent) {
		if (this.battVolt != battVolt | this.battRemain != battRemain
				| this.battCurrent != battCurrent) {
			tts.batteryDischargeNotification(battRemain);
			this.battVolt = battVolt;
			this.battRemain = battRemain;
			this.battCurrent = battCurrent;
			notifyHudUpdate();
		}
	}

	public void setArmedAndFailsafe(boolean armed, boolean failsafe) {
		if (this.armed != armed | this.failsafe != failsafe) {
			if (this.armed != armed) {
				tts.speakArmedState(armed);					
			}
			this.armed = armed;
			this.failsafe = failsafe;
			notifyHudUpdate();
		}
	}

	public void setGpsState(int fix, int satellites_visible) {
		if (satCount != satellites_visible | fixType != fix) {
			if (fixType != fix) {
				tts.speakGpsMode(fix);
			}
			this.fixType = fix;
			this.satCount = satellites_visible;
			notifyHudUpdate();
		}
	}



	public void setType(int type) {
		if (this.type != type) {
			this.type = type;
			notifyTypeChanged();
		}
	}

	public void setMode(ApmModes mode) {
		if (this.mode != mode) {
			this.mode = mode;
			tts.speakMode(mode);
			notifyHudUpdate();
		}
	}

	public void setWpno(int wpno) {
		if (this.wpno != wpno) {
			this.wpno = wpno;
			tts.speak("Going for waypoint "+wpno);
			notifyHudUpdate();
		}
	}

	public void setPosition(LatLng position) {
		if (this.position != position) {
			this.position = position;
			notifyPositionChange();
		}
	}

	private void notifyPositionChange() {
		if (mapListner != null) {
			mapListner.onDroneUpdate();
		}
	}

	private void notifyTypeChanged() {
		if (typeListner != null) {
			typeListner.onDroneTypeChanged();
		}
	}

	private void notifyHudUpdate() {
		if (hudListner != null)
			hudListner.onDroneUpdate();
	}

	public double getRoll() {
		return roll;
	}

	public double getPitch() {
		return pitch;
	}

	public double getYaw() {
		return yaw;
	}

	public double getAltitude() {
		return altitude;
	}

	public double getDisttowp() {
		return disttowp;
	}

	public double getVerticalSpeed() {
		return verticalSpeed;
	}

	public double getGroundSpeed() {
		return groundSpeed;
	}

	public double getAirSpeed() {
		return airSpeed;
	}

	public double getTargetSpeed() {
		return targetSpeed;
	}

	public double getTargetAltitude() {
		return targetAltitude;
	}

	public double getBattVolt() {
		return battVolt;
	}

	public double getBattRemain() {
		return battRemain;
	}

	public double getBattCurrent() {
		return battCurrent;
	}

	public int getWpno() {
		return wpno;
	}

	public int getSatCount() {
		return satCount;
	}

	public int getFixType() {
		return fixType;
	}

	public int getType() {
		return type;
	}

	public ApmModes getMode() {
		return mode;
	}

	public LatLng getPosition() {
		return position;
	}

	public boolean isFailsafe() {
		return failsafe;
	}

	public boolean isArmed() {
		return armed;
	}

	public void setWaypoints(List<waypoint> waypoints) {
		this.waypoints = waypoints;
	}

	public void addWaypoints(List<waypoint> points) {
		waypoints.addAll(points);
	}

	public void addWaypoint(Double Lat, Double Lng, Double h) {
		waypoints.add(new waypoint(Lat, Lng, h));
	}

	public void addWaypoint(LatLng coord, Double h) {
		waypoints.add(new waypoint(coord, h));
	}

	public void addWaypoint(LatLng coord) {
		addWaypoint(coord, getDefaultAlt());
	}

	public void clearWaypoints() {
		waypoints.clear();
	}

	public String getWaypointData() {
		String waypointData = String.format(Locale.ENGLISH, "Home\t%2.0f\n",
				home.Height);
		waypointData += String.format("Def:\t%2.0f\n", getDefaultAlt());

		int i = 1;
		for (waypoint point : waypoints) {
			waypointData += String.format(Locale.ENGLISH, "WP%02d \t%2.0f\n",
					i++, point.Height);
		}
		return waypointData;
	}

	public List<waypoint> getWaypoints() {
		return waypoints;
	}

	public Double getDefaultAlt() {
		return defaultAlt;
	}

	public void setDefaultAlt(Double defaultAlt) {
		this.defaultAlt = defaultAlt;
	}

	public waypoint getHome() {
		return home;
	}

	public waypoint getLastWaypoint() {
		if (waypoints.size() > 0)
			return waypoints.get(waypoints.size() - 1);
		else
			return home;
	}

	public void setHome(waypoint home) {
		this.home = home;
	}

	public void setHome(LatLng home) {
		this.home.coord = home;
	}

	public void moveWaypoint(LatLng coord, int number) {
		waypoints.get(number).coord = coord;
	}

	public List<LatLng> getAllCoordinates() {
		List<LatLng> result = new ArrayList<LatLng>();
		for (waypoint point : waypoints) {
			result.add(point.coord);
		}
		result.add(home.coord);
		return result;
	}

}
