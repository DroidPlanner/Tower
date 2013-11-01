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
	WAYPOINT("Waypoint"), LOITER("Loiter"), LOITERN("LoiterN"), LOITERT(
			"LoiterT"), RTL("RTL"), LAND("Land"), TAKEOFF("Takeoff"), ROI("ROI"), SURVEY("Survey");

	private final String name;

	private MissionItemTypes(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public MissionItem getNewItem(MissionItem item) {
		switch (this) {
		case LAND:
			return new Land(item);
		case LOITER:
			return new LoiterInfinite(item);
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
			return new Survey(item);
		}
		throw new InvalidParameterException();
	}
}