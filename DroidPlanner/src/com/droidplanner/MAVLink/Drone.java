package com.droidplanner.MAVLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.google.android.gms.maps.model.LatLng;

public class Drone {
	public waypoint home;
	public Double defaultAlt;
	public List<waypoint> waypoints;
	
	public double roll = 0, pitch = 0, yaw = 0, altitude = 0, disttowp = 0,
			verticalSpeed = 0, groundSpeed = 0, airSpeed = 0, targetSpeed = 0,
			targetAltitude = 0, battVolt = -1, battRemain = -1,
			battCurrent = -1;
	public int wpno = -1,satCount = -1, fixType = -1, type = MAV_TYPE.MAV_TYPE_FIXED_WING;
	public boolean failsafe = false, armed = false;
	public ApmModes mode = ApmModes.UNKNOWN;
	public LatLng position;
	
	
	HudUpdatedListner hudListner;
	MapUpdatedListner mapListner;
	DroneTypeListner typeListner;
	
	
	public interface HudUpdatedListner{
		public void onDroneUpdate();
	}
	public interface MapUpdatedListner{
		public void onDroneUpdate();
	}
	public interface DroneTypeListner{
		public void onDroneTypeChanged();
	}

	public Drone() {
		super();
		this.home = new waypoint(0.0, 0.0, 0.0);
		this.defaultAlt = 100.0;
		this.waypoints = new ArrayList<waypoint>();
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
	
	public void setHudListner(HudUpdatedListner listner){
		hudListner = listner;
	}
	
	public void setMapListner(MapUpdatedListner listner){
		mapListner = listner;
	}
	
	public void setDroneTypeChangedListner(DroneTypeListner listner){
		typeListner = listner;
	}

	
}
