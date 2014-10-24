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

public class CylindricalSurvey extends MissionItem {

	protected Coord2D center;
	private Length radius;
	private Altitude startHeight, heightStep;
	private int numberOfSteps;
	private boolean crossHatch;

	private CylindricalSurvey(Mission mission) {
		super(mission);
		this.center = null;
		radius = new Length(10.0);
		startHeight = new Altitude(10);
		heightStep = new Altitude(5);
		numberOfSteps = 2;
		crossHatch = false;
	}

	public CylindricalSurvey(Mission mission, Coord2D center) {
		this(mission);
		this.center = center;
	}

	public CylindricalSurvey(MissionItem item) {
		this(item.getMission());
		Coord3D coordinate;
		if (item instanceof SpatialCoordItem) {
			coordinate = ((SpatialCoordItem) item).getCoordinate();
		} else {
			coordinate = new Coord3D(0, 0, new Altitude(0));
		}
		this.center = coordinate;
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
				center, new Altitude(0.0)));
		list.addAll(roi.packMissionItem());
	}

	private void packCircles(List<msg_mission_item> list) {
		for (double altitude = startHeight.valueInMeters(); altitude <= getTopHeight().valueInMeters(); altitude += heightStep.valueInMeters()) {
			Circle circle = new Circle(mission, new Coord3D(center,
					new Altitude(altitude)));
			circle.setRadius(radius.valueInMeters());
			list.addAll(circle.packMissionItem());
		}
	}

	private void packHatch(List<msg_mission_item> list) {
		Polygon polygon = new Polygon();
		for (double angle = 0; angle <= 360; angle += 10) {
			polygon.addPoint(GeoTools.newCoordFromBearingAndDistance(center,
					angle, radius.valueInMeters()));
		}

		Coord2D corner = GeoTools.newCoordFromBearingAndDistance(center,
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
					path.add(GeoTools.newCoordFromBearingAndDistance(center,angle, radius.valueInMeters()));
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
		return new Length(startHeight.valueInMeters()+ (numberOfSteps-1)*heightStep.valueInMeters());
	}

	public Altitude getStartAltitude() {
		return startHeight;
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
		return center;
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

	public void setStartAltitude(int newValue) {
		startHeight = new Altitude(newValue);		
	}

	public void setAltitudeStep(int newValue) {
		heightStep = new Altitude(newValue);		
	}

	public void setNumberOfSteps(int newValue) {
		numberOfSteps = newValue;	
	}

}
