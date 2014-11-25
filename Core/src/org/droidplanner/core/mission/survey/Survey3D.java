package org.droidplanner.core.mission.survey;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.PolygonTools;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.polygon.Polygon;
import org.droidplanner.core.survey.CameraInfo;
import org.droidplanner.core.survey.SurveyData;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

public class Survey3D extends MissionItem {

	public Polygon polygon = new Polygon();
	public SurveyData surveyData = new SurveyData();

	public Survey3D(Mission mission, List<Coord2D> points) {
		super(mission);
		polygon.addPoints(points);
	}

	public void setCameraInfo(CameraInfo camera) {
		surveyData.setCameraInfo(camera);
		mission.notifyMissionUpdate();
	}

	public List<Coord2D> getPath() {
		try {
			List<Coord2D> path = PolygonTools.offsetPolygon(polygon, 10).getPoints();
			return path;
		} catch (Exception e) {
			return new ArrayList<Coord2D>();
		}
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = new ArrayList<msg_mission_item>();
		for (Coord2D point: getPath()) {
			list.add(packSurveyPoint(point, surveyData.getAltitude()));			
		}
		return list;
	}

	public static msg_mission_item packSurveyPoint(Coord2D point, Length altitude) {
		msg_mission_item mavMsg = new msg_mission_item();
		mavMsg.autocontinue = 1;
		mavMsg.target_component = 1;
		mavMsg.target_system = 1;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
		mavMsg.x = (float) point.getX();
		mavMsg.y = (float) point.getY();
		mavMsg.z = (float) altitude.valueInMeters();
		mavMsg.param1 = 0f;
		mavMsg.param2 = 0f;
		mavMsg.param3 = 0f;
		mavMsg.param4 = 0f;
		return mavMsg;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		
	}
	
	@Override
	public MissionItemType getType() {
		return MissionItemType.SURVEY3D;
	}

	public String getCamera() {
		return surveyData.getCameraName();
	}

}
