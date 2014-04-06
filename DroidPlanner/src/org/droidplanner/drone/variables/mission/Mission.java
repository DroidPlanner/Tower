package org.droidplanner.drone.variables.mission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.drone.DroneVariable;
import org.droidplanner.drone.variables.mission.commands.ReturnToHome;
import org.droidplanner.drone.variables.mission.survey.Survey;
import org.droidplanner.drone.variables.mission.waypoints.Land;
import org.droidplanner.drone.variables.mission.waypoints.SpatialCoordItem;
import org.droidplanner.drone.variables.mission.waypoints.Takeoff;
import org.droidplanner.drone.variables.mission.waypoints.Waypoint;
import org.droidplanner.fragments.helpers.MapPath.PathSource;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.helpers.geoTools.GeoTools;
import org.droidplanner.helpers.units.Altitude;
import org.droidplanner.helpers.units.Length;

import android.widget.Toast;

import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.google.android.gms.maps.model.LatLng;

public class Mission extends DroneVariable implements PathSource{

	private List<MissionItem> items = new ArrayList<MissionItem>();
	private List<MissionItem> selection = new ArrayList<MissionItem>();
	private Altitude defaultAlt = new Altitude(20.0);

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
		items.remove(item);
		selection.remove(item);
		notifiyMissionUpdate();
	}

	public void removeWaypoints(List<MissionItem> toRemove) {
		items.removeAll(toRemove);
		selection.removeAll(toRemove);
		notifiyMissionUpdate();		
	}
	
	public void clear() {
	    items.clear();
	    selection.clear();
	    notifiyMissionUpdate();
	}

	public void addWaypoints(List<LatLng> points) {
		Altitude alt = getLastAltitude();
		for (LatLng point : points) {
			items.add(new Waypoint(this, point,alt));
		}		
		notifiyMissionUpdate();
	}

	public void addWaypoint(LatLng point) {
		items.add(new Waypoint(this,point,getLastAltitude()));
		notifiyMissionUpdate();
	}

	public void notifiyMissionUpdate() {
		myDrone.events.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
	}

	private Altitude getLastAltitude() {
		Altitude alt;
		try{
			SpatialCoordItem lastItem = (SpatialCoordItem) items.get(items.size()-1);
			alt = lastItem.getAltitude();
		}catch (Exception e){
			alt = defaultAlt;			
		}
		return alt;
	}
	
	public void replace(MissionItem oldItem, MissionItem newItem) {
		int index = items.indexOf(oldItem);
		if (selectionContains(oldItem)) {
			removeItemFromSelection(oldItem);
			addToSelection(newItem);
		}
		items.remove(index);
		items.add(index, newItem);
		notifiyMissionUpdate();		
	}

	public void reverse() {
		Collections.reverse(items);
		notifiyMissionUpdate();	
	}

	/**
	 * Moves the selected objects up or down into the mission listing
	 * 
	 * Think of it as pushing the selected objects, while you can only move a
	 * single unselected object per turn.
	 * 
	 * @param moveUp
	 *            true to move up, but can be false to move down
	 */
	public void moveSelection(boolean moveUp) {
		if (selection.size() > 0 | selection.size() < items.size()) {
			Collections.sort(selection);
			if (moveUp) {
				Collections.rotate(getSublistToRotateUp(), 1);				
			}else{
				Collections.rotate(getSublistToRotateDown(), -1);
			}
			notifiyMissionUpdate();
		}
	}

	private List<MissionItem> getSublistToRotateUp() {
		int from = items.indexOf(selection.get(0));
		int to = from;
		do{
			if (items.size() < to + 2)
				return items.subList(0, 0);
		}while (selection.contains(items.get(++to)));
		return items.subList(from, to + 1); // includes one unselected item
	}

	private List<MissionItem> getSublistToRotateDown() {
		int from = items.indexOf(selection.get(selection.size() - 1));
		int to = from;
		do {
			if (to < 1) {
				return items.subList(0, 0);
			}
		} while (selection.contains(items.get(--to)));
		return items.subList(to, from + 1); // includes one unselected item
	}

	public void addSurveyPolygon(List<LatLng> points) {
		Survey survey = new Survey(this, points, myDrone.context);
		items.add(survey);
		notifiyMissionUpdate();		
	}

	public void onWriteWaypoints(msg_mission_ack msg) {
		Toast.makeText(myDrone.context, "Waypoints sent", Toast.LENGTH_SHORT)
				.show();
		myDrone.tts.speak("Waypoints saved to Drone");
	}

	@Override
	public List<LatLng> getPathPoints() {
		List<LatLng> newPath = new ArrayList<LatLng>();
		for (MissionItem item : items) {
			try {
				newPath.addAll(item.getPath());
			} catch (Exception e) {
				// Exception when no path for the item
			}
		}
		return newPath;
	}

	public List<MissionItem> getItems() {
		return items;
	}

	public List<MarkerSource> getMarkers() {
		List<MarkerSource> markers = new ArrayList<MarkerSource>();
		for (MissionItem item : items) {
			try {
				markers.addAll(item.getMarkers());
			} catch (Exception e) {
				// Exception when no markers for the item
			}
		}
		return markers;
	}

	public Integer getNumber(MissionItem waypoint) {
		return items.indexOf(waypoint)+1; // plus one to account for the fact that this is an index
	}

	public Length getAltitudeDiffFromPreviousItem(
            SpatialCoordItem waypoint) throws Exception {
		int i = items.indexOf(waypoint);
		if (i > 0) {
			MissionItem previus = items.get(i - 1);
			if (previus instanceof SpatialCoordItem) {
				return waypoint.getAltitude().subtract(
						((SpatialCoordItem) previus).getAltitude());
			}
		}
		throw new Exception("Last waypoint doesn't have an altitude");
	}

	public Length getDistanceFromLastWaypoint(SpatialCoordItem waypoint) throws Exception {
		int i = items.indexOf(waypoint);
		if (i > 0) {
			MissionItem previus = items.get(i - 1);
			if (previus instanceof SpatialCoordItem) {
				return GeoTools.getDistance(waypoint.getCoordinate(),
						((SpatialCoordItem) previus).getCoordinate());
			}
		}
		throw new Exception("Last waypoint doesn't have a coordinate");
	}

	public boolean hasItem(MissionItem item) {
		return items.contains(item);
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
			myDrone.home.setHome(msgs.get(0));
			msgs.remove(0); // Remove Home waypoint
			selection.clear();
			items.clear();
			items.addAll(processMavLinkMessages(msgs));
			myDrone.events.notifyDroneEvent(DroneEventsType.MISSION_RECEIVED);
			notifiyMissionUpdate();
		}
	}

	private List<MissionItem> processMavLinkMessages(List<msg_mission_item> msgs) {
		List<MissionItem> received = new ArrayList<MissionItem>();
		
		for (msg_mission_item msg : msgs) {
			switch (msg.command) {
			case MAV_CMD.MAV_CMD_NAV_WAYPOINT:
				received.add(new Waypoint(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_TAKEOFF:
				received.add(new Takeoff(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_LAND:
				received.add(new Land(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH:
				received.add(new ReturnToHome(msg, this));
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
		for (MissionItem item : items) {
			data.addAll(item.packMissionItem());			
		}				
		myDrone.waypointMananger.writeWaypoints(data);
	}

	public void addMissionUpdatesListner(
			OnDroneListener listner) {
		myDrone.events.addDroneListener(listner);
	}

	public void removeMissionUpdatesListner(
			OnDroneListener listener) {
		myDrone.events.removeDroneListener(listener);		
	}

}
