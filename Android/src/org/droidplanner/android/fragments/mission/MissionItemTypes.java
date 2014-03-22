package org.droidplanner.android.fragments.mission;

import java.security.InvalidParameterException;

import org.droidplanner.android.graphic.GraphicWaypoint;
import org.droidplanner.core.mission.MissionItem;


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
		case WAYPOINT:
			return new GraphicWaypoint(item);
		default:
			throw new InvalidParameterException();
		}
	}

	class InvalidItemException extends Exception{
		private static final long serialVersionUID = 1L;

	}
}