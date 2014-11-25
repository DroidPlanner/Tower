package org.droidplanner.core.mission.survey;

import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.polygon.Polygon;
import org.droidplanner.core.survey.CameraInfo;
import org.droidplanner.core.survey.SurveyData;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

public abstract class Survey extends MissionItem {

	public Polygon polygon = new Polygon();
	public SurveyData surveyData = new SurveyData();

	protected Survey(Mission mission) {
		super(mission);
	}
	
	protected Survey(MissionItem item) {
		super(item);
	}
	
	protected Survey(Mission mission, List<Coord2D> points) {
		super(mission);
		polygon.addPoints(points);
	}

	public void setCameraInfo(CameraInfo camera) {
		surveyData.setCameraInfo(camera);
		mission.notifyMissionUpdate();
	}

	public String getCamera() {
		return surveyData.getCameraName();
	}
	
	public abstract void build() throws Exception;
	
	@Override
	public final void unpackMAVMessage(msg_mission_item mavMsg) {	
	}	
	
	public static msg_mission_item packSurveyPoint(Coord2D point, Length altitude) {
		msg_mission_item mavMsg = new msg_mission_item();
		mavMsg.autocontinue = 1;
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

}