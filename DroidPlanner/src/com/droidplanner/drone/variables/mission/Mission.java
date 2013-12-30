package com.droidplanner.drone.variables.mission;

import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;

import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.variables.mission.survey.Survey;
import com.droidplanner.drone.variables.mission.waypoints.SpatialCoordItem;
import com.droidplanner.drone.variables.mission.waypoints.Waypoint;
import com.droidplanner.fragments.helpers.MapPath.PathSource;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.helpers.geoTools.GeoTools;
import com.droidplanner.helpers.units.Altitude;
import com.droidplanner.helpers.units.Length;
import com.google.android.gms.maps.model.LatLng;

public class Mission extends DroneVariable implements PathSource{

	private List<MissionItem> itens = new ArrayList<MissionItem>();
	private List<MissionItem> selection = new ArrayList<MissionItem>();
	private Altitude defaultAlt = new Altitude(50.0);

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
		myDrone.events.notifyDroneEvent(DroneEventsType.MISSION);
	}

	public void removeWaypoints(List<MissionItem> toRemove) {
		itens.removeAll(toRemove);
		selection.removeAll(toRemove);
		myDrone.events.notifyDroneEvent(DroneEventsType.MISSION);		
	}

	public void addWaypoints(List<LatLng> points) {
		Altitude alt = getLastAltitude();
		for (LatLng point : points) {
			itens.add(new Waypoint(this, point,alt));
		}		
		myDrone.events.notifyDroneEvent(DroneEventsType.MISSION);
	}

	public void addWaypoint(LatLng point) {
		itens.add(new Waypoint(this,point,getLastAltitude()));
		myDrone.events.notifyDroneEvent(DroneEventsType.MISSION);
	}

	private Altitude getLastAltitude() {
		Altitude alt;
		try{
			SpatialCoordItem lastItem = (SpatialCoordItem) itens.get(itens.size()-1);
			alt = lastItem.getAltitude();
		}catch (Exception e){
			alt = defaultAlt;			
		}
		return alt;
	}
	
	public void replace(MissionItem oldItem, MissionItem newItem) {
		int index = itens.indexOf(oldItem);
		if (selectionContains(oldItem)) {
			removeItemFromSelection(oldItem);
			addToSelection(newItem);
		}
		itens.remove(index);
		itens.add(index, newItem);
		myDrone.events.notifyDroneEvent(DroneEventsType.MISSION);		
	}

	public void addSurveyPolygon(List<LatLng> points) {
		Survey survey = new Survey(this, points, myDrone.context);
		itens.add(survey);
		myDrone.events.notifyDroneEvent(DroneEventsType.MISSION);		
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

	public Length getDistanceFromLastWaypoint(SpatialCoordItem waypoint) throws Exception {
		int i = itens.indexOf(waypoint);
		if (i > 0) {
			MissionItem previus = itens.get(i - 1);
			if (previus instanceof SpatialCoordItem) {
				return GeoTools.getDistance(waypoint.getCoordinate(),
						((SpatialCoordItem) previus).getCoordinate());
			}
		}
		throw new Exception("Last waypoint doesn't have a coordinate");
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

	public void onMissionReceived(List<msg_mission_item> msgs) {
		if (msgs != null) {
			Toast.makeText(myDrone.context, "Waypoints received from Drone",
					Toast.LENGTH_SHORT).show();
			myDrone.tts.speak("Waypoints received");
			myDrone.home.setHome(msgs.get(0));
			msgs.remove(0); // Remove Home waypoint
			selection.clear();
			itens.clear();
			itens.addAll(processMavLinkMessages(msgs));
			myDrone.events.notifyDroneEvent(DroneEventsType.MISSION);
		}
	}

	private List<MissionItem> processMavLinkMessages(List<msg_mission_item> msgs) {
		List<MissionItem> received = new ArrayList<MissionItem>();
		
		for (msg_mission_item msg : msgs) {
			switch (msg.command) {
			case MAV_CMD.MAV_CMD_NAV_WAYPOINT:
				received.add(new Waypoint(msg, this));
				break;
			default:
				break;
			}
		}		
		return received;
	}

	public void sendMissionToAPM() {
		List<msg_mission_item> data = new ArrayList<msg_mission_item>();
		data.add(myDrone.home.packMavlink());
		for (MissionItem item : itens) {
			data.add(item.packMissionItem());			
		}				
		myDrone.waypointMananger.writeWaypoints(data);
	}

}
