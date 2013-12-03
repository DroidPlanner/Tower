package com.droidplanner.drone.variables.mission;

import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;

import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.OnWaypointChangedListner;
import com.droidplanner.drone.DroneVariable;
import com.droidplanner.drone.variables.mission.survey.Survey;
import com.droidplanner.drone.variables.mission.waypoints.SpatialCoordItem;
import com.droidplanner.drone.variables.mission.waypoints.Waypoint;
import com.droidplanner.fragments.helpers.MapPath.PathSource;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.helpers.units.Altitude;
import com.droidplanner.helpers.units.Length;
import com.google.android.gms.maps.model.LatLng;

public class Mission extends DroneVariable implements PathSource{

	private List<MissionItem> itens = new ArrayList<MissionItem>();
	private List<MissionItem> selection = new ArrayList<MissionItem>();
	private Altitude defaultAlt = new Altitude(50.0);


	private List<OnWaypointChangedListner> missionListner = new ArrayList<OnWaypointChangedListner>();

	public Mission(Drone myDrone) {
		super(myDrone);
	}

	public Altitude getDefaultAlt() {
		return defaultAlt;
	}

	public void setDefaultAlt(Altitude newAltitude) {
		defaultAlt = newAltitude;		
	}

	public void removeWaypoint(MissionItem item) {
		itens.remove(item);
		selection.remove(item);
		onMissionUpdate();
	}

	public void removeWaypoints(List<MissionItem> toRemove) {
		itens.removeAll(toRemove);
		selection.removeAll(toRemove);
		onMissionUpdate();		
	}

	public void addWaypointsWithDefaultAltitude(List<LatLng> points) {
		for (LatLng point : points) {
			itens.add(new Waypoint(this, point,defaultAlt));
		}		
		onMissionUpdate();
	}

	public void addWaypoint(LatLng point, Altitude alt) {
		itens.add(new Waypoint(this,point,alt));
		onMissionUpdate();
	}
	
	public void replace(MissionItem oldItem, MissionItem newItem) {
		int index = itens.indexOf(oldItem);
		if (selectionContains(oldItem)) {
			removeItemFromSelection(oldItem);
			addToSelection(newItem);
		}
		itens.remove(index);
		itens.add(index, newItem);
		onMissionUpdate();		
	}

	public void addSurveyPolygon(List<LatLng> points) {
		Survey survey = new Survey(this, points, myDrone.context);
		itens.add(survey);
		onMissionUpdate();		
	}

	public void addOnMissionUpdateListner(OnWaypointChangedListner listner) {
		if (!missionListner.contains(listner)) {
			missionListner.add(listner);
		}
		
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
		for (MissionItem item : itens) {
			try {
				newPath.addAll(item.getPath());
			} catch (Exception e) {
				// Exception when no path for the item
			}
		}
		return newPath;
	}

	public List<MissionItem> getItems() {
		return itens;
	}

	public List<MarkerSource> getMarkers() {
		List<MarkerSource> markers = new ArrayList<MarkerSource>();
		for (MissionItem item : itens) {
			try {
				markers.addAll(item.getMarkers());
			} catch (Exception e) {
				// Exception when no markers for the item
			}
		}
		return markers;
	}

	public void onMissionUpdate() {
		for (OnWaypointChangedListner listner : missionListner) {
			if (listner!=null) {
				listner.onMissionUpdate();
			}
		}
	}

	public void removeOnMissionUpdateListner(
			OnWaypointChangedListner listner) {
		missionListner.remove(listner);
	}

	public Integer getNumber(MissionItem waypoint) {
		return itens.indexOf(waypoint)+1; // plus one to account for the fact that this is an index
	}

	public Length getAltitudeDiffFromPreviusItem(
			SpatialCoordItem waypoint) throws Exception {
		int i = itens.indexOf(waypoint);
		if (i > 0) {
			MissionItem previus = itens.get(i - 1);
			if (previus instanceof SpatialCoordItem) {
				return waypoint.getAltitude().subtract(
						((SpatialCoordItem) previus).getAltitude());
			}
		}
		throw new Exception("Last waypoint doesn't have an altitude");
	}

	public boolean hasItem(MissionItem item) {
		return itens.contains(item);
	}

	public void clearSelection() {
		selection.clear();		
	}

	public boolean selectionContains(MissionItem item) {
		return selection.contains(item);
	}

	public void addToSelection(List<MissionItem> items) {
		selection.addAll(items);		
	}

	public void addToSelection(MissionItem item) {
		selection.add(item);		
	}

	public void setSelectionTo(MissionItem item) {
		selection.clear();
		selection.add(item);
	}

	public void removeItemFromSelection(MissionItem item) {
		selection.remove(item);		
	}

	public List<MissionItem> getSelected() {
		return selection;
	}


}
