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
	private int type = MAV_TYPE.MAV_TYPE_FIXED_WING;
		
	public DroneGPS GPS = new DroneGPS(this,-1, -1, -1);
	public DroneSpeed speed = new DroneSpeed(this,0, 0, 0, 0);
	public DroneState state = new DroneState(this,false, false, ApmModes.UNKNOWN);
	public DroneBattery battery = new DroneBattery(this,-1, -1,-1);	
	public DroneMission mission = new DroneMission(this,-1,0);	
	public DroneAltitude altitude = new DroneAltitude(this,0, 0);
	public DroneOrientation orientation = new DroneOrientation(this,0, 0, 0);
	
	
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
		this.mission.home = new waypoint(0.0, 0.0, 0.0);
		this.mission.defaultAlt = 50.0;
		this.mission.waypoints = new ArrayList<waypoint>();
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
		this.orientation.roll = roll;
		this.orientation.pitch = pitch;
		this.orientation.yaw = yaw;
		notifyHudUpdate();
	}

	public void setAltitudeGroundAndAirSpeeds(double altitude,
			double groundSpeed, double airSpeed, double climb) {
		this.altitude.altitude = altitude;
		this.speed.groundSpeed = groundSpeed;
		this.speed.airSpeed = airSpeed;
		this.speed.verticalSpeed = climb;
		notifyHudUpdate();
	}

	public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error,
			double aspd_error) {
		this.mission.disttowp = disttowp;
		altitude.targetAltitude = alt_error + altitude.altitude;
		speed.targetSpeed = aspd_error + speed.airSpeed;
		notifyHudUpdate();
	}

	public void setBatteryState(double battVolt, double battRemain,
			double battCurrent) {
		if (this.battery.battVolt != battVolt | this.battery.battRemain != battRemain
				| this.battery.battCurrent != battCurrent) {
			tts.batteryDischargeNotification(battRemain);
			this.battery.battVolt = battVolt;
			this.battery.battRemain = battRemain;
			this.battery.battCurrent = battCurrent;
			notifyHudUpdate();
		}
	}

	public void setArmedAndFailsafe(boolean armed, boolean failsafe) {
		if (this.state.armed != armed | this.state.failsafe != failsafe) {
			if (this.state.armed != armed) {
				tts.speakArmedState(armed);					
			}
			this.state.armed = armed;
			this.state.failsafe = failsafe;
			notifyHudUpdate();
		}
	}

	public void setGpsState(int fix, int satellites_visible, int eph) {
		if (GPS.satCount != satellites_visible | GPS.fixType != fix) {
			if (GPS.fixType != fix) {
				tts.speakGpsMode(fix);
			}
			this.GPS.fixType = fix;
			this.GPS.satCount = satellites_visible;
			this.GPS.gps_eph = (double) eph / 100; //convert from eph(cm) to gps_eph(m)
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
		if (this.state.mode != mode) {
			this.state.mode = mode;
			tts.speakMode(mode);
			notifyHudUpdate();
		}
	}

	public void setWpno(int wpno) {
		if (this.mission.wpno != wpno) {
			this.mission.wpno = wpno;
			tts.speak("Going for waypoint "+wpno);
			notifyHudUpdate();
		}
	}

	public void setPosition(LatLng position) {
		if (this.GPS.position != position) {
			this.GPS.position = position;
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

	public int getType() {
		return type;
	}


	public void setWaypoints(List<waypoint> waypoints) {
		this.mission.waypoints = waypoints;
	}

	public void addWaypoints(List<waypoint> points) {
		mission.waypoints.addAll(points);
	}

	public void addWaypoint(Double Lat, Double Lng, Double h) {
		mission.waypoints.add(new waypoint(Lat, Lng, h));
	}

	public void addWaypoint(LatLng coord, Double h) {
		mission.waypoints.add(new waypoint(coord, h));
	}

	public void addWaypoint(LatLng coord) {
		addWaypoint(coord, mission.getDefaultAlt());
	}

	public void clearWaypoints() {
		mission.waypoints.clear();
	}

	public void setDefaultAlt(Double defaultAlt) {
		this.mission.defaultAlt = defaultAlt;
	}

	public String getWaypointData() {
		String waypointData = String.format(Locale.ENGLISH, "Home\t%2.0f\n",
				mission.home.Height);
		waypointData += String.format("Def:\t%2.0f\n", mission.getDefaultAlt());
	
		int i = 1;
		for (waypoint point : mission.waypoints) {
			waypointData += String.format(Locale.ENGLISH, "WP%02d \t%2.0f\n",
					i++, point.Height);
		}
		return waypointData;
	}

	public List<waypoint> getWaypoints() {
		return mission.waypoints;
	}

	public void setHome(waypoint home) {
		this.mission.home = home;
	}

	public void setHome(LatLng home) {
		this.mission.home.coord = home;
	}

	public void moveWaypoint(LatLng coord, int number) {
		mission.waypoints.get(number).coord = coord;
	}

}
