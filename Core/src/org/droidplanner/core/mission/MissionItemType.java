package org.droidplanner.core.mission;

import org.droidplanner.core.mission.commands.CameraTrigger;
import org.droidplanner.core.mission.commands.ChangeSpeed;
import org.droidplanner.core.mission.commands.ConditionYaw;
import org.droidplanner.core.mission.commands.EpmGripper;
import org.droidplanner.core.mission.commands.ReturnToHome;
import org.droidplanner.core.mission.commands.SetServo;
import org.droidplanner.core.mission.commands.Takeoff;
import org.droidplanner.core.mission.survey.Survey2D;
import org.droidplanner.core.mission.survey.Survey3D;
import org.droidplanner.core.mission.waypoints.Circle;
import org.droidplanner.core.mission.waypoints.Land;
import org.droidplanner.core.mission.waypoints.RegionOfInterest;
import org.droidplanner.core.mission.waypoints.SplineWaypoint;
import org.droidplanner.core.mission.waypoints.Waypoint;

public enum MissionItemType {
	WAYPOINT("Waypoint"), SPLINE_WAYPOINT("Spline Waypoint"), TAKEOFF("Takeoff"), RTL(
			"Return to Launch"), LAND("Land"), CIRCLE("Circle"), ROI("Region of Interest"), SURVEY2D(
			"Survey"), CHANGE_SPEED("Change Speed"), CAMERA_TRIGGER("Camera Trigger"), EPM_GRIPPER("EPM"), SET_SERVO("Set Servo"), CONDITION_YAW("Set Yaw"), SURVEY3D("Structure Scanner");

	private final String name;

	private MissionItemType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
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
		case CAMERA_TRIGGER:
			return new CameraTrigger(referenceItem);
		case EPM_GRIPPER:
			return new EpmGripper(referenceItem);
		case RTL:
			return new ReturnToHome(referenceItem);
		case LAND:
			return new Land(referenceItem);
		case CIRCLE:
			return new Circle(referenceItem);
		case ROI:
			return new RegionOfInterest(referenceItem);
		case SURVEY2D:
			return new Survey2D(referenceItem);
		case SURVEY3D:
			return new Survey3D(referenceItem);
		case SET_SERVO:
			return new SetServo(referenceItem);
		case CONDITION_YAW:
			return new ConditionYaw(referenceItem);
		default:
			throw new IllegalArgumentException("Unrecognized mission item type (" + name + ")"
					+ ".");
		}
	}
}
