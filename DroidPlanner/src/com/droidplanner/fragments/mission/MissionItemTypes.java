package com.droidplanner.fragments.mission;

import java.security.InvalidParameterException;

import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.drone.variables.mission.commands.ReturnToHome;
import com.droidplanner.drone.variables.mission.survey.Survey;
import com.droidplanner.drone.variables.mission.waypoints.Land;
import com.droidplanner.drone.variables.mission.waypoints.LoiterInfinite;
import com.droidplanner.drone.variables.mission.waypoints.LoiterTime;
import com.droidplanner.drone.variables.mission.waypoints.LoiterTurns;
import com.droidplanner.drone.variables.mission.waypoints.RegionOfInterest;
import com.droidplanner.drone.variables.mission.waypoints.Takeoff;
import com.droidplanner.drone.variables.mission.waypoints.Waypoint;

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

	public MissionItem getNewItem(MissionItem item) throws InvalidItemException {
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