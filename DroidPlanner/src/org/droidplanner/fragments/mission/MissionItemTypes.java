package org.droidplanner.fragments.mission;

import java.security.InvalidParameterException;

import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.drone.variables.mission.commands.ReturnToHome;
import org.droidplanner.drone.variables.mission.waypoints.Land;
import org.droidplanner.drone.variables.mission.waypoints.LoiterTime;
import org.droidplanner.drone.variables.mission.waypoints.LoiterTurns;
import org.droidplanner.drone.variables.mission.waypoints.RegionOfInterest;
import org.droidplanner.drone.variables.mission.waypoints.Takeoff;
import org.droidplanner.drone.variables.mission.waypoints.Waypoint;
import org.droidplanner.drone.variables.missionD.MissionItemD;


public enum MissionItemTypes {
		WAYPOINT("Waypoint"),
		TAKEOFF("Takeoff"),
		RTL("Return to Launch"),
		LAND("Land"),
		LOITERN("Circle"),
		LOITERT("Loiter"),
		//LOITER("Loiter indefinitly"),
		ROI("Region of Interest"),
		SURVEY("Survey");

	private final String name;

	private MissionItemTypes(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public MissionItem getNewItem(MissionItemD item) throws InvalidItemException {
		switch (this) {
		case LAND:
			return new Land(item);
		//case LOITER:
		//	return new LoiterInfinite(item);
		case LOITERN:
			return new LoiterTurns(item);
		case LOITERT:
			return new LoiterTime(item);
		case ROI:
			return new RegionOfInterest(item);
		case RTL:
			return new ReturnToHome(item);
		case TAKEOFF:
			return new Takeoff(item);
		case WAYPOINT:
			return new Waypoint(item);
		case SURVEY:
			throw new InvalidItemException();
		}
		throw new InvalidParameterException();
	}

	class InvalidItemException extends Exception{
		private static final long serialVersionUID = 1L;

	}
}