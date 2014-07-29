package org.droidplanner.core.mission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.math.MathUtil;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.commands.Takeoff;
import org.droidplanner.core.mission.survey.Survey;
import org.droidplanner.core.mission.survey.grid.Grid;
import org.droidplanner.core.mission.waypoints.Circle;
import org.droidplanner.core.mission.waypoints.Land;
import org.droidplanner.core.mission.waypoints.RegionOfInterest;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;
import org.droidplanner.core.mission.waypoints.SplineWaypoint;
import org.droidplanner.core.mission.waypoints.Waypoint;
import org.droidplanner.core.parameters.Parameter;

import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

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

	/**
	 * Signals that this mission object was updated. //TODO: maybe move outside
	 * of this class
	 */
	public void notifyMissionUpdate() {
		myDrone.events.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
	}

	/**
	 * @return the altitude of the last added mission item.
	 */
	public Altitude getLastAltitude() {
		Altitude alt;
		try {
			SpatialCoordItem lastItem = (SpatialCoordItem) items.get(items.size() - 1);
			alt = lastItem.getCoordinate().getAltitude();
		} catch (Exception e) {
			alt = defaultAlt;
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
		int index = items.indexOf(oldItem);
		items.remove(index);
		items.add(index, newItem);
		notifyMissionUpdate();
	}

	/**
	 * Reverse the order of the mission items.
	 */
	public void reverse() {
		Collections.reverse(items);
		notifyMissionUpdate();
	}

	public void onWriteWaypoints(msg_mission_ack msg) {
		myDrone.events.notifyDroneEvent(DroneEventsType.MISSION_SENT);
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

	public MissionItem getPreviousItem(MissionItem item) {
		int i = items.indexOf(item);
		if (i > 0) {
			return items.get(i - 1);
		}
		return null;
	}

	public boolean hasItem(MissionItem item) {
		return items.contains(item);
	}

	public void onMissionReceived(List<msg_mission_item> msgs) {
		if (msgs != null) {
			myDrone.home.setHome(msgs.get(0));
			msgs.remove(0); // Remove Home waypoint
			items.clear();
			items.addAll(processMavLinkMessages(msgs));
			myDrone.events.notifyDroneEvent(DroneEventsType.MISSION_RECEIVED);
			notifyMissionUpdate();
		}
	}

	public void onMissionLoaded(List<msg_mission_item> msgs) {
		if (msgs != null) {
			myDrone.home.setHome(msgs.get(0));
			msgs.remove(0); // Remove Home waypoint
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

			case MAV_CMD.MAV_CMD_NAV_SPLINE_WAYPOINT:
				received.add(new SplineWaypoint(msg, this));
				break;

			case MAV_CMD.MAV_CMD_NAV_LAND:
				received.add(new Land(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_TAKEOFF:
				received.add(new Takeoff(msg, this));
				break;
			case MAV_CMD.MAV_CMD_DO_SET_ROI:
				received.add(new RegionOfInterest(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_LOITER_TURNS:
				received.add(new Circle(msg, this));
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
		myDrone.waypointManager.writeWaypoints(getMsgMissionItems());
	}

	public List<msg_mission_item> getMsgMissionItems() {
		final List<msg_mission_item> data = new ArrayList<msg_mission_item>();
		data.add(myDrone.home.packMavlink());
		for (MissionItem item : items) {
			data.addAll(item.packMissionItem());
		}
		return data;
	}

	public static void dist(Drone drone2, Length dist, List<MissionItem> waypoints) {
		for (MissionItem waypoint : waypoints) {
			Mission mission = waypoint.getMission();
			MissionItem previousWaypoint = mission.getPreviousItem((MissionItem) waypoint);
			Coord2D previousWaypointCoordinate = null;
	
			try {
				if ((SpatialCoordItem) previousWaypoint != null) {
					previousWaypointCoordinate = ((SpatialCoordItem) previousWaypoint)
							.getCoordinate();
				}
			} catch (ClassCastException c) {
				// Some missionitems, like RTL, have no associated coordinate
			}
	
			if (previousWaypoint instanceof Survey) {
				if (((Survey) previousWaypoint).getGrid() != null) {
					List<Coord2D> gridPoints = ((Survey) previousWaypoint).getGrid().gridPoints;
					previousWaypointCoordinate = gridPoints.get(gridPoints.size() - 1);
				}
			}
			switch (waypoint.getType()) {
			case SPLINE_WAYPOINT:
			case WAYPOINT:
				Length altDelta = new Length(0.0),
				distDelta = new Length(0.0);
				try {
					altDelta = drone2.mission
							.getAltitudeDiffFromPreviousItem((SpatialCoordItem) waypoint);
					if (previousWaypoint != null) {
						distDelta = GeoTools.getDistance(previousWaypointCoordinate,
								((SpatialCoordItem) waypoint).getCoordinate());
					}
				} catch (IllegalArgumentException e) {// if this is the first
														// waypoint after a
														// takeoff, this
														// happens. Use drone
														// "home" only if
														// available, else just
														// ignore from
														// calculations.
					if (drone2.home.isValid()) {
						Coord2D home = drone2.home.getCoord();
						Coord3D waypointCoordinate = ((SpatialCoordItem) waypoint).getCoordinate();
						altDelta = new Length(waypointCoordinate.getAltitude().valueInMeters());
						distDelta = GeoTools.getDistance(home, waypointCoordinate);
					}
				}
				dist.add(MathUtil.hypot(altDelta, distDelta));
				break;
			case TAKEOFF:
				dist.add(drone2.mission.getDefaultAlt());
				break;
			case LAND:
				dist.add(drone2.mission.getLastAltitude());
				break;
			case CIRCLE:
				// Add the circumferences (2*PI*r), but subtract twice the
				// radius, b/c the drone never actually travels to/from the
				// center of the circle. It stops at the edge of the circle and
				// begins strafing. Also add all altitude steps. And remember to
				// multiply each circumference by the number of turns it does
				Circle circle = (Circle) waypoint;
				Length altDelta2 = new Length(0.0),
				distDelta2 = new Length(0.0);
				try {
					altDelta2 = drone2.mission
							.getAltitudeDiffFromPreviousItem((SpatialCoordItem) waypoint);
					if (previousWaypoint != null) {
						distDelta2 = GeoTools.getDistance(previousWaypointCoordinate,
								((SpatialCoordItem) waypoint).getCoordinate());
					}
					distDelta2.addMeters(-1 * circle.getRadius());
				} catch (IllegalArgumentException e) {// if this is the first
														// waypoint after a
														// takeoff, this
														// happens. Use drone
														// "home" only if
														// available, else just
														// ignore from
														// calculations.
					if (drone2.home.isValid()) {
						Coord2D home = drone2.home.getCoord();
						Coord3D waypointCoordinate = ((SpatialCoordItem) waypoint).getCoordinate();
						altDelta2 = new Length(waypointCoordinate.getAltitude().valueInMeters());
						distDelta2 = GeoTools.getDistance(home, waypointCoordinate);
					}
				}
				dist.add(MathUtil.hypot(altDelta2, distDelta2));
				dist.addMeters(-1 * circle.getRadius());
				for (int step = 0; step < circle.getNumberOfSteps(); step++) {
					double circumference = circle.getRadius() * 2 * Math.PI;
					dist.addMeters(circumference * circle.getNumberOfTurns());
					dist.addMeters(circle.getAltitudeStep());
				}
				break;
			case RTL:
				// first, change altitude to rTLALT
				double rTLAlt = 15.0;// default RTL value in case we haven't
										// loaded this param yet
				Parameter prefAlt = drone2.parameters.getParameter("RTL_ATL");
				if (prefAlt != null) {
					rTLAlt = prefAlt.value / 10.0;// it's in centimeters
				}
				double lastAltitude = waypoint.getMission().getLastAltitude().valueInMeters();
				Length altDelta3 = new Length(Math.abs(lastAltitude - rTLAlt));
				dist.add(altDelta3);
				// then, travel back to home
				if (drone2.home.isValid()) {
					Coord2D home = drone2.home.getCoord();
					if (previousWaypoint != null) {
						dist.add(GeoTools.getDistance(home, previousWaypointCoordinate));
					}
				}
				// now, land from the rTLALT
				dist.addMeters(rTLAlt);
				break;
			case SURVEY:
				Survey survey = (Survey) waypoint;
				Grid surveyGrid = survey.getGrid();
				if (previousWaypoint != null) {
					if (surveyGrid != null && surveyGrid.gridPoints != null
							&& surveyGrid.gridPoints.size() > 0 && previousWaypoint != null) {
						Coord2D startOfSurvey = surveyGrid.gridPoints.get(0);
						dist.add(GeoTools.getDistance(previousWaypointCoordinate, startOfSurvey));
					}
	
				}
				if (surveyGrid != null) {
					List<Coord2D> surveyGridWaypoints = survey.getGrid().getGridPoints();
					for (int i = 0; i < surveyGridWaypoints.size() - 1; i++) {
						dist.add(GeoTools.getDistance(surveyGridWaypoints.get(i),
								surveyGridWaypoints.get(i + 1)));
					}
				}
				break;
			default:
				break;
	
			}
		}
	}
}
