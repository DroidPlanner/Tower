package org.droidplanner.core.mission.survey;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.survey.grid.GridBuilder;
import org.droidplanner.core.mission.waypoints.Circle;
import org.droidplanner.core.mission.waypoints.RegionOfInterest;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;
import org.droidplanner.core.polygon.Polygon;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

public class CylindricalSurvey extends SpatialCoordItem {
	private Length radius = new Length(10.0);
	private Altitude heightStep = new Altitude(5);
	private int numberOfSteps = 2;
	private boolean crossHatch = false;

	public CylindricalSurvey(Mission mission, Coord3D coord) {
		super(mission,coord);
	}

	public CylindricalSurvey(MissionItem item) {
		super(item);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = new ArrayList<msg_mission_item>();
		packROI(list);
		packCircles(list);
		if (crossHatch) {
			packHatch(list);			
		}
		return list;
	}

	private void packROI(List<msg_mission_item> list) {
		RegionOfInterest roi = new RegionOfInterest(mission, new Coord3D(
				coordinate, new Altitude(0.0)));
		list.addAll(roi.packMissionItem());
	}

	private void packCircles(List<msg_mission_item> list) {
		for (double altitude = coordinate.getAltitude().valueInMeters(); altitude <= getTopHeight().valueInMeters(); altitude += heightStep.valueInMeters()) {
			Circle circle = new Circle(mission, new Coord3D(coordinate,	new Altitude(altitude)));
			circle.setRadius(radius.valueInMeters());
			list.addAll(circle.packMissionItem());
		}
	}

	private void packHatch(List<msg_mission_item> list) {
		Polygon polygon = new Polygon();
		for (double angle = 0; angle <= 360; angle += 10) {
			polygon.addPoint(GeoTools.newCoordFromBearingAndDistance(coordinate,
					angle, radius.valueInMeters()));
		}

		Coord2D corner = GeoTools.newCoordFromBearingAndDistance(coordinate,
				-45, radius.valueInMeters()*2);
		GridBuilder grid = new GridBuilder(polygon, 0.0,
				radius.valueInMeters() / 4, corner );
		try {
			for (Coord2D point : grid.generate(false).gridPoints) {
				list.add(Survey.packSurveyPoint(point, getTopHeight()));
			}
			grid.setAngle(90.0);
			for (Coord2D point : grid.generate(false).gridPoints) {
				list.add(Survey.packSurveyPoint(point, getTopHeight()));
			}
		} catch (Exception e) { // Should never fail, since it has good polygons
		}

	}

	public List<Coord2D> getPath() {
		List<Coord2D> path = new ArrayList<Coord2D>();
		for (msg_mission_item msg_mission_item : packMissionItem()) {
			if (msg_mission_item.command == MAV_CMD.MAV_CMD_NAV_WAYPOINT) {
				path.add(new Coord2D(msg_mission_item.x, msg_mission_item.y));
			}
			if (msg_mission_item.command == MAV_CMD.MAV_CMD_NAV_LOITER_TURNS) {
				for (double angle = 0; angle <= 360; angle += 12) {
					path.add(GeoTools.newCoordFromBearingAndDistance(coordinate,angle, radius.valueInMeters()));
				}
			}
			
		}
		return path;

	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.CYLINDRICAL_SURVEY;
	}
	


	private Length getTopHeight() {
		return new Length(coordinate.getAltitude().valueInMeters()+ (numberOfSteps-1)*heightStep.valueInMeters());
	}

	public Altitude getEndAltitude() {
		return heightStep;
	}

	public int getNumberOfSteps() {
		return numberOfSteps;
	}

	public Length getRadius() {
		return radius;
	}

	public Coord2D getCenter() {
		return coordinate;
	}

	public void setRadius(int newValue) {
		radius = new Length(newValue);
	}

	public void enableCrossHatch(boolean isEnabled) {
		crossHatch = isEnabled;
	}

	public boolean isCrossHatchEnabled() {
		return crossHatch;
	}

	public void setAltitudeStep(int newValue) {
		heightStep = new Altitude(newValue);		
	}

	public void setNumberOfSteps(int newValue) {
		numberOfSteps = newValue;	
	}

}
