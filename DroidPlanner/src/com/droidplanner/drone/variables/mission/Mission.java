package com.droidplanner.drone.variables.mission;

import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;

import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.droidplanner.DroidPlannerApp.OnWaypointChangedListner;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;
import com.droidplanner.drone.variables.mission.waypoints.GenericWaypoint;
import com.droidplanner.drone.variables.mission.waypoints.Waypoint;
import com.droidplanner.fragments.helpers.MapPath.PathSource;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.google.android.gms.maps.model.LatLng;

public class Mission extends DroneVariable implements PathSource,
		OnWaypointChangedListner {

	private List<MissionItem> waypoints = new ArrayList<MissionItem>();
	private Double defaultAlt = 50.0;

	private List<OnWaypointChangedListner> missionListner = new ArrayList<OnWaypointChangedListner>();

	public Mission(Drone myDrone) {
		super(myDrone);
	}

	public double getDefaultAlt() {
		return defaultAlt;
	}

	public void setDefaultAlt(double newAltitude) {
		defaultAlt = newAltitude;		
	}

	public void removeWaypoint(GenericWaypoint waypoint) {
		waypoints.remove(waypoint);
		onMissionUpdate();
	}

	public void addWaypointsWithDefaultAltitude(List<LatLng> points) {
		for (LatLng point : points) {
			addWaypoint(point,defaultAlt);
		}		
	}

	public void addWaypoint(LatLng point, Double alt) {
		waypoints.add(new Waypoint(point,alt));
	}

	public void onMissionReceived(List<msg_mission_item> mission) {
		throw new IllegalArgumentException("NOT implemented"); //TODO implement this
		/*
		if (mission != null) {
			Toast.makeText(myDrone.context, "Waypoints received from Drone",
					Toast.LENGTH_SHORT).show();
			myDrone.tts.speak("Waypoints received");
			home.updateData(mission.get(0));
			mission.remove(0); // Remove Home waypoint
			clearWaypoints();
			addWaypoints(mission);
			onMissionUpdate();
			myDrone.notifyDistanceToHomeChange();
		}
		*/
	}

	public void sendMissionToAPM() {
		throw new IllegalArgumentException("NOT implemented"); //TODO implement this
		/*
		List<Waypoint> data = new ArrayList<Waypoint>();
		data.add(myDrone.home.getHome());
		data.addAll(getWaypoints());
		myDrone.waypointMananger.writeWaypoints(data);
		*/
	}

	public void onWriteWaypoints(msg_mission_ack msg) {
		Toast.makeText(myDrone.context, "Waypoints sent", Toast.LENGTH_SHORT)
				.show();
		myDrone.tts.speak("Waypoints saved to Drone");
	}

	@Override
	public List<LatLng> getPathPoints() {
		List<LatLng> newPath = new ArrayList<LatLng>();
		for (MissionItem item : waypoints) {
			try {
				newPath.addAll(item.getPath());
			} catch (Exception e) {
				// Exception when no path for the item
			}
		}
		return newPath;
	}

	public List<MissionItem> getItems() {
		return waypoints;
	}

	public List<MarkerSource> getMarkers() {
		List<MarkerSource> markers = new ArrayList<MarkerSource>();
		for (MissionItem item : waypoints) {
			try {
				markers.addAll(item.getMarkers());
			} catch (Exception e) {
				// Exception when no markers for the item
			}
		}
		return markers;
	}

	@Override
	public void onMissionUpdate() {
		for (OnWaypointChangedListner listner : missionListner) {
			if (listner!=null) {
				listner.onMissionUpdate();
			}
		}
	}

	public void addOnMissionUpdateListner(OnWaypointChangedListner listner) {
		if (!missionListner.contains(listner)) {
			missionListner.add(listner);
		}
		
	}

	public void removeOnMissionUpdateListner(
			OnWaypointChangedListner listner) {
		missionListner.remove(listner);
	}

}
