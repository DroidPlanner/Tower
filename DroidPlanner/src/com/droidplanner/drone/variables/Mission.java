package com.droidplanner.drone.variables;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.widget.Toast;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.droidplanner.DroidPlannerApp.OnWaypointReceivedListner;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;
import com.google.android.gms.maps.model.LatLng;

public class Mission extends DroneVariable {

	private waypoint home = new waypoint(0.0, 0.0, 0.0);
	private List<waypoint> waypoints = new ArrayList<waypoint>();
	private Double defaultAlt = 50.0;
	private int wpno = -1;
	private double disttowp = 0;
	
	public OnWaypointReceivedListner waypointsListner;

	public Mission(Drone myDrone) {
		super(myDrone);
	}

	public double getDisttowp() {
		return disttowp;
	}

	public int getWpno() {
		return wpno;
	}

	public Double getDefaultAlt() {
		return defaultAlt;
	}

	public waypoint getHome() {
		return home;
	}

	public void setDistanceToWp(double disttowp) {
		this.disttowp = disttowp;
	}

	public waypoint getLastWaypoint() {
		if (waypoints.size() > 0)
			return waypoints.get(waypoints.size() - 1);
		else
			return home;
	}

	public List<LatLng> getAllCoordinates() {
		List<LatLng> result = new ArrayList<LatLng>();
		for (waypoint point : waypoints) {
			result.add(point.coord);
		}
		result.add(home.coord);
		return result;
	}

	public void setWpno(int wpno) {
		if (this.wpno != wpno) {
			this.wpno = wpno;
			myDrone.tts.speak("Going for waypoint " + wpno);
			myDrone.notifyHudUpdate();
		}
	}

	public void setWaypoints(List<waypoint> waypoints) {
		this.waypoints = waypoints;
	}

	public void addWaypoints(List<waypoint> points) {
		waypoints.addAll(points);
	}

	public void addWaypoint(Double lat, Double Lng, Double h) {
		waypoints.add(new waypoint(lat, Lng, h));
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

	public void setDefaultAlt(Double defaultAlt) {
		this.defaultAlt = defaultAlt;
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

	public void setHome(waypoint home) {
		this.home = home;
	}

	public void setHome(LatLng home) {
		this.home.coord = home;
	}

	public void moveWaypoint(LatLng coord, int number) {
		waypoints.get(number).coord = coord;
	}

	public void onWaypointsReceived(List<waypoint> waypoints) {
		if (waypoints != null) {
			Toast.makeText(myDrone.context,"Waypoints received from Drone", Toast.LENGTH_SHORT).show();
			myDrone.tts.speak("Waypoints received");
			setHome(waypoints.get(0));
			waypoints.remove(0); // Remove Home waypoint
			clearWaypoints();
			addWaypoints(waypoints);
			waypointsListner.onWaypointsReceived();
		}

	}

	public void onWriteWaypoints(msg_mission_ack msg) {
		Toast.makeText(myDrone.context, "Waypoints sent", Toast.LENGTH_SHORT).show();
		myDrone.tts.speak("Waypoints saved to Drone");
	}
}