package org.droidplanner.core.mission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.survey.Survey;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;
import org.droidplanner.core.mission.waypoints.Waypoint;

import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

public class Mission extends DroneVariable{

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
		notifyMissionUpdate();
	}

	public void removeWaypoints(List<MissionItem> toRemove) {
		items.removeAll(toRemove);
		selection.removeAll(toRemove);
		notifyMissionUpdate();
	}

	public void addWaypoints(List<Coord2D> points) {
		Altitude alt = getLastAltitude();
		for (Coord2D point : points) {
			items.add(new Waypoint(this, new Coord3D(point, alt)));
		}		
		notifyMissionUpdate();
	}

	public void addWaypoint(Coord2D point) {
		items.add(new Waypoint(this, new Coord3D(point, getLastAltitude())));
		notifyMissionUpdate();
	}

	public void notifyMissionUpdate() {
		myDrone.events.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
	}

	private Altitude getLastAltitude() {
		Altitude alt;
		try{
			SpatialCoordItem lastItem = (SpatialCoordItem) items.get(items.size()-1);
			alt = lastItem.getCoordinate().getAltitude();
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
		notifyMissionUpdate();
	}

	public void reverse() {
		Collections.reverse(items);
		notifyMissionUpdate();
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
				Collections.rotate(getSubListToRotateUp(), 1);
			}else{
				Collections.rotate(getSubListToRotateDown(), -1);
			}
			notifyMissionUpdate();
		}
	}

	private List<MissionItem> getSubListToRotateUp() {
		int from = items.indexOf(selection.get(0));
		int to = from;
		do{
			if (items.size() < to + 2)
				return items.subList(0, 0);
		}while (selection.contains(items.get(++to)));
		return items.subList(from, to + 1); // includes one unselected item
	}

	private List<MissionItem> getSubListToRotateDown() {
		int from = items.indexOf(selection.get(selection.size() - 1));
		int to = from;
		do {
			if (to < 1) {
				return items.subList(0, 0);
			}
		} while (selection.contains(items.get(--to)));
		return items.subList(to, from + 1); // includes one unselected item
	}

	public void addSurveyPolygon(List<Coord2D> points) {
		Survey survey = new Survey(this, points);
		items.add(survey);
		notifyMissionUpdate();
	}

	public void onWriteWaypoints(msg_mission_ack msg) {
		myDrone.events.notifyDroneEvent(DroneEventsType.MISSION_SENT);
	}

	public List<MissionItem> getItems() {
		return items;
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
				return waypoint.getCoordinate().getAltitude().subtract(
						((SpatialCoordItem) previus).getCoordinate().getAltitude());
			}
		}
		throw new Exception("Last waypoint doesn't have an altitude");
	}

	public Length getDistanceFromLastWaypoint(SpatialCoordItem waypoint) throws Exception {
		int i = items.indexOf(waypoint);
		if (i > 0) {
			MissionItem previous = items.get(i - 1);
			if (previous instanceof SpatialCoordItem) {
				return GeoTools.getDistance(waypoint.getCoordinate(),
						((SpatialCoordItem) previous).getCoordinate());
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
			myDrone.home.setHome(msgs.get(0));
			msgs.remove(0); // Remove Home waypoint
			selection.clear();
			items.clear();
			items.addAll(processMavLinkMessages(msgs));
			myDrone.events.notifyDroneEvent(DroneEventsType.MISSION_RECEIVED);
			notifyMissionUpdate();
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
		for (MissionItem item : items) {
			data.addAll(item.packMissionItem());			
		}				
		myDrone.waypointManager.writeWaypoints(data);
	}

	public void addMissionUpdatesListener(
            OnDroneListener listener) {
		myDrone.events.addDroneListener(listener);
	}

	public void removeMissionUpdatesListener(
            OnDroneListener listener) {
		myDrone.events.removeDroneListener(listener);		
	}

}
