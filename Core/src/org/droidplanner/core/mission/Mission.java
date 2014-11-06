package org.droidplanner.core.mission;

import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.helpers.units.Speed;
import org.droidplanner.core.mission.commands.CameraTrigger;
import org.droidplanner.core.mission.commands.ChangeSpeed;
import org.droidplanner.core.mission.commands.ConditionYaw;
import org.droidplanner.core.mission.commands.EpmGripper;
import org.droidplanner.core.mission.commands.ReturnToHome;
import org.droidplanner.core.mission.commands.SetServo;
import org.droidplanner.core.mission.commands.Takeoff;
import org.droidplanner.core.mission.waypoints.Circle;
import org.droidplanner.core.mission.waypoints.Land;
import org.droidplanner.core.mission.waypoints.RegionOfInterest;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;
import org.droidplanner.core.mission.waypoints.SplineWaypoint;
import org.droidplanner.core.mission.waypoints.Waypoint;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This implements a mavlink mission. A mavlink mission is a set of
 * commands/mission items to be carried out by the drone. TODO: rename the
 * 'waypoint' method to 'missionItem' (i.e: addMissionItem)
 */
public class Mission extends DroneVariable {

	/**
	 * Stores the set of mission items belonging to this mission.
	 */
	private List<MissionItem> items = new ArrayList<MissionItem>();
	private Altitude defaultAlt = new Altitude(20.0);

	public Mission(Drone myDrone) {
		super(myDrone);
	}

	/**
	 * @return the mission's default altitude
	 */
	public Altitude getDefaultAlt() {
		return defaultAlt;
	}

	/**
	 * Sets the mission default altitude.
	 * 
	 * @param newAltitude
	 *            {@link Altitude} value
	 */
	public void setDefaultAlt(Altitude newAltitude) {
		defaultAlt = newAltitude;
	}

	/**
	 * Removes a waypoint from the mission's set of mission items.
	 * 
	 * @param item
	 *            waypoint to remove
	 */
	public void removeWaypoint(MissionItem item) {
		items.remove(item);
		notifyMissionUpdate();
	}

	/**
	 * Removes a list of waypoints from the mission's set of mission items.
	 * 
	 * @param toRemove
	 *            list of waypoints to remove
	 */
	public void removeWaypoints(List<MissionItem> toRemove) {
		items.removeAll(toRemove);
		notifyMissionUpdate();
	}

	/**
	 * Add a list of waypoints to the mission's set of mission items.
	 * 
	 * @param missionItems
	 *            list of waypoints to add
	 */
	public void addMissionItems(List<MissionItem> missionItems) {
		items.addAll(missionItems);
		notifyMissionUpdate();
	}

	/**
	 * Add a waypoint to the mission's set of mission item.
	 * 
	 * @param missionItem
	 *            waypoint to add
	 */
	public void addMissionItem(MissionItem missionItem) {
		items.add(missionItem);
		notifyMissionUpdate();
	}

	public void addMissionItem(int index, MissionItem missionItem) {
		items.add(index, missionItem);
		notifyMissionUpdate();
	}

	/**
	 * Signals that this mission object was updated. //TODO: maybe move outside
	 * of this class
	 */
	public void notifyMissionUpdate() {
		myDrone.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
	}

	/**
	 * @return the altitude of the last added mission item.
	 */
	public Altitude getLastAltitude() {
		Altitude alt = defaultAlt;
		try {
			SpatialCoordItem lastItem = (SpatialCoordItem) items.get(items.size() - 1);
			if (!(lastItem instanceof RegionOfInterest)) {
				alt = lastItem.getCoordinate().getAltitude();
			}
		} catch (Exception e) {
		}
		return alt;
	}

	/**
	 * Updates a mission item
	 * 
	 * @param oldItem
	 *            mission item to update
	 * @param newItem
	 *            new mission item
	 */
	public void replace(MissionItem oldItem, MissionItem newItem) {
		final int index = items.indexOf(oldItem);
		if (index == -1) {
			return;
		}

		items.remove(index);
		items.add(index, newItem);
		notifyMissionUpdate();
	}

	public void replaceAll(List<Pair<MissionItem, MissionItem>> updatesList) {
		if (updatesList == null || updatesList.isEmpty()) {
			return;
		}

		boolean wasUpdated = false;
		for (Pair<MissionItem, MissionItem> updatePair : updatesList) {
			final MissionItem oldItem = updatePair.first;
			final int index = items.indexOf(oldItem);
			if (index == -1) {
				continue;
			}

			final MissionItem newItem = updatePair.second;
			items.remove(index);
			items.add(index, newItem);

			wasUpdated = true;
		}

		if (wasUpdated) {
			notifyMissionUpdate();
		}
	}

	/**
	 * Reverse the order of the mission items.
	 */
	public void reverse() {
		Collections.reverse(items);
		notifyMissionUpdate();
	}

	public void onWriteWaypoints(msg_mission_ack msg) {
		myDrone.notifyDroneEvent(DroneEventsType.MISSION_SENT);
	}

	public List<MissionItem> getItems() {
		return items;
	}

	public int getOrder(MissionItem waypoint) {
		return items.indexOf(waypoint) + 1; // plus one to account for the fact
											// that this is an index
	}

	public Length getAltitudeDiffFromPreviousItem(SpatialCoordItem waypoint)
			throws IllegalArgumentException {
		int i = items.indexOf(waypoint);
		if (i > 0) {
			MissionItem previous = items.get(i - 1);
			if (previous instanceof SpatialCoordItem) {
				return waypoint.getCoordinate().getAltitude()
						.subtract(((SpatialCoordItem) previous).getCoordinate().getAltitude());
			}
		}
		throw new IllegalArgumentException("Last waypoint doesn't have an altitude");
	}

	public Length getDistanceFromLastWaypoint(SpatialCoordItem waypoint)
			throws IllegalArgumentException {
		int i = items.indexOf(waypoint);
		if (i > 0) {
			MissionItem previous = items.get(i - 1);
			if (previous instanceof SpatialCoordItem) {
				return GeoTools.getDistance(waypoint.getCoordinate(),
						((SpatialCoordItem) previous).getCoordinate());
			}
		}
		throw new IllegalArgumentException("Last waypoint doesn't have a coordinate");
	}

	public boolean hasItem(MissionItem item) {
		return items.contains(item);
	}

	public void onMissionReceived(List<msg_mission_item> msgs) {
		if (msgs != null) {
			myDrone.getHome().setHome(msgs.get(0));
			msgs.remove(0); // Remove Home waypoint
			items.clear();
			items.addAll(processMavLinkMessages(msgs));
			myDrone.notifyDroneEvent(DroneEventsType.MISSION_RECEIVED);
			notifyMissionUpdate();
		}
	}

	public void onMissionLoaded(List<msg_mission_item> msgs) {
		if (msgs != null) {
			myDrone.getHome().setHome(msgs.get(0));
			msgs.remove(0); // Remove Home waypoint
			items.clear();
			items.addAll(processMavLinkMessages(msgs));
			myDrone.notifyDroneEvent(DroneEventsType.MISSION_RECEIVED);
			notifyMissionUpdate();
		}
	}

	private List<MissionItem> processMavLinkMessages(List<msg_mission_item> msgs) {
		List<MissionItem> received = new ArrayList<MissionItem>();

		for (msg_mission_item msg : msgs) {
			switch (msg.command) {
			case MAV_CMD.MAV_CMD_DO_SET_SERVO:
				received.add(new SetServo(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_WAYPOINT:
				received.add(new Waypoint(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_SPLINE_WAYPOINT:
				received.add(new SplineWaypoint(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_LAND:
				received.add(new Land(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_TAKEOFF:
				received.add(new Takeoff(msg, this));
				break;
			case MAV_CMD.MAV_CMD_DO_CHANGE_SPEED:
				received.add(new ChangeSpeed(msg, this));
				break;
			case MAV_CMD.MAV_CMD_DO_SET_CAM_TRIGG_DIST:
				received.add(new CameraTrigger(msg,this));
				break;
			case EpmGripper.MAV_CMD_DO_GRIPPER:
				received.add(new EpmGripper(msg, this));
				break;
			case MAV_CMD.MAV_CMD_DO_SET_ROI:
				received.add(new RegionOfInterest(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_LOITER_TURNS:
				received.add(new Circle(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH:
				received.add(new ReturnToHome(msg, this));
				break;				
			case MAV_CMD.MAV_CMD_CONDITION_YAW:
				received.add(new ConditionYaw(msg, this));
				break;
			default:
				break;
			}
		}
		return received;
	}

	/**
	 * Sends the mission to the drone using the mavlink protocol.
	 */
	public void sendMissionToAPM() {
		myDrone.getWaypointManager().writeWaypoints(getMsgMissionItems());
	}

	public List<msg_mission_item> getMsgMissionItems() {
		final List<msg_mission_item> data = new ArrayList<msg_mission_item>();
		data.add(myDrone.getHome().packMavlink());
		for (MissionItem item : items) {
			data.addAll(item.packMissionItem());
		}
		return data;
	}

	/**
	 * Create and upload a dronie mission to the drone
	 * 
	 * @return the bearing in degrees the drone trajectory will take.
	 */
	public double makeAndUploadDronie() {
		Coord2D currentPosition = myDrone.getGps().getPosition();
		if (currentPosition == null || myDrone.getGps().getSatCount() <= 5) {
			myDrone.notifyDroneEvent(DroneEventsType.WARNING_NO_GPS);
			return -1;
		}

		final double bearing = 180 + myDrone.getOrientation().getYaw();
		items.clear();
		items.addAll(createDronie(currentPosition,
				GeoTools.newCoordFromBearingAndDistance(currentPosition, bearing, 50.0)));
		sendMissionToAPM();
		notifyMissionUpdate();

		return bearing;
	}

	public List<MissionItem> createDronie(Coord2D start, Coord2D end) {
		final int startAltitude = 4;
		final int roiDistance = -8;
		Coord2D slowDownPoint = GeoTools.pointAlongTheLine(start, end, 5);

		Speed defaultSpeed = myDrone.getSpeed().getSpeedParameter();
		if (defaultSpeed == null) {
			defaultSpeed = new Speed(5);
		}

		List<MissionItem> dronieItems = new ArrayList<MissionItem>();
		dronieItems.add(new Takeoff(this, new Altitude(startAltitude)));
		dronieItems.add(new RegionOfInterest(this, new Coord3D(GeoTools.pointAlongTheLine(start,
				end, roiDistance), new Altitude(1.0))));
		dronieItems.add(new Waypoint(this, new Coord3D(end, new Altitude(startAltitude
				+ GeoTools.getDistance(start, end).valueInMeters() / 2.0))));
		dronieItems.add(new Waypoint(this, new Coord3D(slowDownPoint, new Altitude(startAltitude
				+ GeoTools.getDistance(start, slowDownPoint).valueInMeters() / 2.0))));
		dronieItems.add(new ChangeSpeed(this, new Speed(1.0)));
		dronieItems.add(new Waypoint(this, new Coord3D(start, new Altitude(startAltitude))));
		dronieItems.add(new ChangeSpeed(this, defaultSpeed));
		dronieItems.add(new Land(this, start));
		return dronieItems;
	}

	public boolean hasTakeoffAndLandOrRTL() {
		if (items.size() >= 2) {
			if (isFirstItemTakeoff() && isLastItemLandOrRTL()) {
				return true;
			}
		}
		return false;
	}

	public boolean isFirstItemTakeoff() {
		return !items.isEmpty() && items.get(0) instanceof Takeoff;
	}

	public boolean isLastItemLandOrRTL() {
        if(items.isEmpty())
            return false;

		MissionItem last = items.get(items.size() - 1);
		return (last instanceof ReturnToHome) || (last instanceof Land);
	}
}
