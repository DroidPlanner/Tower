package org.droidplanner.core.mission.survey;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.commands.CameraTrigger;
import org.droidplanner.core.polygon.Polygon;
import org.droidplanner.core.survey.CameraInfo;
import org.droidplanner.core.survey.SurveyData;
import org.droidplanner.core.survey.grid.Grid;
import org.droidplanner.core.survey.grid.GridBuilder;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

public class Survey2D extends MissionItem {

	public Polygon polygon = new Polygon();
	public SurveyData surveyData = new SurveyData();
	public Grid grid;

	public Survey2D(Mission mission, List<Coord2D> points) {
		super(mission);
		polygon.addPoints(points);
	}

	public void update(double angle, Altitude altitude, double overlap, double sidelap) {
		surveyData.update(angle, altitude, overlap, sidelap);
		mission.notifyMissionUpdate();
	}

	public void setCameraInfo(CameraInfo camera) {
		surveyData.setCameraInfo(camera);
		mission.notifyMissionUpdate();
	}

	public void build() throws Exception {
		// TODO find better point than (0,0) to reference the grid
		grid = null;
		GridBuilder gridBuilder = new GridBuilder(polygon, surveyData, new Coord2D(0, 0));
		polygon.checkIfValid();
		grid = gridBuilder.generate(true);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		try {
			List<msg_mission_item> list = new ArrayList<msg_mission_item>();
			build();

			list.addAll((new CameraTrigger(mission, surveyData.getLongitudinalPictureDistance())).packMissionItem());
			packGridPoints(list);
			list.addAll((new CameraTrigger(mission, new Length(0.0)).packMissionItem()));
			
			return list;
		} catch (Exception e) {
			return new ArrayList<msg_mission_item>();
		}
	}

	private void packGridPoints(List<msg_mission_item> list) {
		for (Coord2D point : grid.gridPoints) {
			msg_mission_item mavMsg = packSurveyPoint(point,surveyData.getAltitude());
			list.add(mavMsg);
		}
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

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		// TODO Auto-generated method stub

	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.SURVEY;
	}

}
