package com.droidplanner.drone.variables;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.widget.Toast;

import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;
import com.droidplanner.fragments.helpers.MapPath.PathSource;
import com.google.android.gms.maps.model.LatLng;

public class Mission extends DroneVariable implements PathSource, OnWaypointUpdateListner {

	private Home home = new Home();
	private List<waypoint> waypoints = new ArrayList<waypoint>();
	private Double defaultAlt = 50.0;
	private int wpno = -1;
	private double disttowp = 0;

	public OnWaypointUpdateListner missionListner;

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

	public Home getHome() {
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

	public List<LatLng> getAllVisibleCoordinates() {
		List<LatLng> result = new ArrayList<LatLng>();
		for (waypoint point : waypoints) {
			if (point.getCmd().showOnMap()  && point.hasCoord()) {
				result.add(point.getCoord());				
			}
		}
		if (home.isValid && home.hasCoord()) {
			result.add(home.getCoord());
		}
		return result;
	}

	public void setWpno(int wpno) {
		if (this.wpno != wpno) {
			this.wpno = wpno;
			myDrone.tts.speak("Going for waypoint " + wpno);
			myDrone.notifyHudUpdate();
		}
	}

	public void reNumberWaypoints() {
		int i=1;
		for (waypoint wp : waypoints) {
			wp.setNumber(i++);			
		}
	}
	
	public void setWaypoints(List<waypoint> waypoints) {
		this.waypoints.clear();
		addWaypoints(waypoints);
	}

	public void addWaypoints(List<waypoint> points) {
		
		//Loop through every wp and add it to the wp list
		for (waypoint wp : points) {
			addWaypoint(wp);
		}
//		waypoints.addAll(points);
	}

	public void addWaypointsWithDefaultAltitude(List<LatLng> coords) {
		for (LatLng coord : coords) {
			addWaypoint(coord);
		}
	}

	private void addWaypoint(waypoint wp) {
		wp.setNumber(waypoints.size() + 1);
		waypoints.add(wp);
	}

	public void addWaypoint(Double lat, Double Lng, Double h) {
		addWaypoint(new waypoint(lat, Lng, h));
	}

	public void addWaypoint(LatLng coord, Double h) {
		addWaypoint(new waypoint(coord, h));
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
				home.getHeight());
		waypointData += String.format("Def:\t%2.0f\n", getDefaultAlt());

		int i = 1;
		for (waypoint point : waypoints) {
			waypointData += String.format(Locale.ENGLISH, "WP%02d \t%2.0f\n",
					i++, point.getHeight());
		}
		return waypointData;
	}

	public List<waypoint> getWaypoints() {
		return waypoints;
	}

	public void setHome(Home home) {
		this.home = home;
	}

	public void setHome(LatLng home) {
		this.home.setCoord(home);
	}
	
	public void moveWaypoint(LatLng coord, int number) {
		waypoints.get(number).setCoord(coord);
	}

	public void onWaypointsReceived(List<waypoint> waypoints) {
		if (waypoints != null) {
			Toast.makeText(myDrone.context, "Waypoints received from Drone",
					Toast.LENGTH_SHORT).show();
			myDrone.tts.speak("Waypoints received");
			home.updateData(waypoints.get(0));
			waypoints.remove(0); // Remove Home waypoint
			clearWaypoints();
			addWaypoints(waypoints);
			onWaypointsUpdate();
		}

	}

	public void onWriteWaypoints(msg_mission_ack msg) {
		Toast.makeText(myDrone.context, "Waypoints sent", Toast.LENGTH_SHORT)
				.show();
		myDrone.tts.speak("Waypoints saved to Drone");
	}

	public void removeWaypoint(waypoint waypoint) {
		waypoints.remove(waypoint);
		onWaypointsUpdate();
	}

	public void sendMissionToAPM() {
		List<waypoint> data = new ArrayList<waypoint>();
		data.add(getHome());
		data.addAll(getWaypoints());
		myDrone.waypointMananger.writeWaypoints(data);
	}

	@Override
	public List<LatLng> getPathPoints() {
		List<LatLng> newPath = new ArrayList<LatLng>();
		for (waypoint point : getWaypoints()) {
			if (point.getCmd().isOnFligthPath()) {
				newPath.add(point.getCoord());				
			}
		}
		return newPath;
	}

	@Override
	public void onWaypointsUpdate() {
		missionListner.onWaypointsUpdate();
	}

}
