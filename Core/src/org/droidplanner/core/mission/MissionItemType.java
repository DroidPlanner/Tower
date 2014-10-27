package org.droidplanner.core.mission;

import java.util.Collections;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.mission.commands.ChangeSpeed;
import org.droidplanner.core.mission.commands.ReturnToHome;
import org.droidplanner.core.mission.commands.Takeoff;
import org.droidplanner.core.mission.survey.CylindricalSurvey;
import org.droidplanner.core.mission.survey.Survey;
import org.droidplanner.core.mission.waypoints.Circle;
import org.droidplanner.core.mission.waypoints.Land;
import org.droidplanner.core.mission.waypoints.RegionOfInterest;
import org.droidplanner.core.mission.waypoints.SplineWaypoint;
import org.droidplanner.core.mission.waypoints.Waypoint;

public enum MissionItemType {
	WAYPOINT("Waypoint"), SPLINE_WAYPOINT("Spline Waypoint"), TAKEOFF("Takeoff"), RTL(
			"Return to Launch"), LAND("Land"), CIRCLE("Circle"), ROI("Region of Interest"), SURVEY(
			"Survey"), CYLINDRICAL_SURVEY("Structure Scan"), CHANGE_SPEED("Change Speed");

	private final String label;

	private MissionItemType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

    public static MissionItemType fromLabel(String label){
        if(WAYPOINT.getLabel().equals(label))
            return WAYPOINT;

        if(SPLINE_WAYPOINT.getLabel().equals(label))
            return SPLINE_WAYPOINT;

        if(TAKEOFF.getLabel().equals(label))
            return TAKEOFF;

        if(RTL.getLabel().equals(label))
            return RTL;

        if(LAND.getLabel().equals(label))
            return LAND;

        if(CIRCLE.getLabel().equals(label))
            return CIRCLE;

        if(ROI.getLabel().equals(label))
            return ROI;

        if(SURVEY.getLabel().equals(label))
            return SURVEY;

        if(CYLINDRICAL_SURVEY.getLabel().equals(label))
            return CYLINDRICAL_SURVEY;

        if(CHANGE_SPEED.getLabel().equals(label))
            return CHANGE_SPEED;

        return null;
    }

	public MissionItem getNewItem(MissionItem referenceItem) throws IllegalArgumentException {
		switch (this) {
		case WAYPOINT:
			return new Waypoint(referenceItem);
		case SPLINE_WAYPOINT:
			return new SplineWaypoint(referenceItem);
		case TAKEOFF:
			return new Takeoff(referenceItem);
		case CHANGE_SPEED:
			return new ChangeSpeed(referenceItem);
		case RTL:
			return new ReturnToHome(referenceItem);
		case LAND:
			return new Land(referenceItem);
		case CIRCLE:
			return new Circle(referenceItem);
		case ROI:
			return new RegionOfInterest(referenceItem);
		case SURVEY:
			return new Survey(referenceItem.getMission(), Collections.<Coord2D> emptyList());
		case CYLINDRICAL_SURVEY:
			return new CylindricalSurvey(referenceItem);
		default:
			throw new IllegalArgumentException("Unrecognized mission item type (" + label + ")"
					+ ".");
		}
	}
}
