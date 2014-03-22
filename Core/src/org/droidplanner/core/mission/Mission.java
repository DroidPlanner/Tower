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

	private List<MissionItem> itens = new ArrayList<MissionItem>();
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
		itens.remove(item);
		selection.remove(item);
		notifiyMissionUpdate();
	}

	public void removeWaypoints(List<MissionItem> toRemove) {
		itens.removeAll(toRemove);
		selection.removeAll(toRemove);
		notifiyMissionUpdate();		
	}

	public void addWaypoints(List<Coord2D> points) {
		Altitude alt = getLastAltitude();
		for (Coord2D point : points) {
			itens.add(new Waypoint(this, new Coord3D(point,alt)));
		}		
		notifiyMissionUpdate();
	}

	public void addWaypoint(Coord2D point) {
		itens.add(new Waypoint(this,new Coord3D(point,getLastAltitude())));
		notifiyMissionUpdate();
	}

	public void notifiyMissionUpdate() {
		myDrone.events.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
	}

	private Altitude getLastAltitude() {
		Altitude alt;
		try{
			SpatialCoordItem lastItem = (SpatialCoordItem) itens.get(itens.size()-1);
			alt = lastItem.getCoordinate().getAltitude();
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
		notifiyMissionUpdate();		
	}

	public void reverse() {
		Collections.reverse(itens);
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
		if (selection.size() > 0 | selection.size() < itens.size()) {
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
		int from = itens.indexOf(selection.get(0));
		int to = from;
		do{
			if (itens.size() < to + 2)
				return itens.subList(0, 0);
		}while (selection.contains(itens.get(++to)));
		return itens.subList(from, to + 1); // includes one unselected item
	}

	private List<MissionItem> getSublistToRotateDown() {
		int from = itens.indexOf(selection.get(selection.size() - 1));
		int to = from;
		do {
			if (to < 1) {
				return itens.subList(0, 0);
			}
		} while (selection.contains(itens.get(--to)));
		return itens.subList(to, from + 1); // includes one unselected item
	}

	public void addSurveyPolygon(List<Coord2D> points) {
		Survey survey = new Survey(this, points);
		itens.add(survey);
		notifiyMissionUpdate();		
	}

	public void onWriteWaypoints(msg_mission_ack msg) {
		myDrone.events.notifyDroneEvent(DroneEventsType.MISSION_SENT);
	}

	public List<MissionItem> getItems() {
		return itens;
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
				return waypoint.getCoordinate().getAltitude().subtract(
						((SpatialCoordItem) previus).getCoordinate().getAltitude());
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
			myDrone.home.setHome(msgs.get(0));
			msgs.remove(0); // Remove Home waypoint
			selection.clear();
			itens.clear();
			itens.addAll(processMavLinkMessages(msgs));
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
