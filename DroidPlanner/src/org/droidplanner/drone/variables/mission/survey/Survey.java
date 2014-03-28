package org.droidplanner.drone.variables.mission.survey;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.drone.variables.mission.Mission;
import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.drone.variables.mission.survey.grid.Grid;
import org.droidplanner.drone.variables.mission.survey.grid.GridBuilder;
import org.droidplanner.file.IO.CameraInfo;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionSurveyFragment;
import org.droidplanner.helpers.units.Altitude;
import org.droidplanner.polygon.Polygon;

import android.content.Context;
import android.widget.Toast;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_FRAME;
import com.google.android.gms.maps.model.LatLng;

public class Survey extends MissionItem {

	public Polygon polygon = new Polygon();
	public SurveyData surveyData = new SurveyData();
	public Grid grid;
	private Context context;
	
	public Survey(Mission m) {
	    super(m);
	}

	public Survey(Mission mission,List<LatLng> points, Context context) {
		this(mission);
		this.context = context;
		polygon.addPoints(points);
	}
	
	public void update(double angle, Altitude altitude, double overlap,
			double sidelap) {
		surveyData.update(angle, altitude, overlap, sidelap);
		mission.notifiyMissionUpdate();
	}

	public void setCameraInfo(CameraInfo camera) {
		surveyData.setCameraInfo(camera);
		mission.notifiyMissionUpdate();
	}
	

	@Override
	public List<LatLng> getPath() throws Exception {
			build();
			return grid.getCameraLocations();
	}

	private void build() throws Exception {		
		try {
		//TODO find better point than (0,0) to reference the grid
		GridBuilder gridBuilder = new GridBuilder(polygon, surveyData, new LatLng(0, 0),context);
		polygon.checkIfValid(context);
		grid = gridBuilder.generate();
		grid.setAltitude(surveyData.getAltitude());
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
			throw new Exception();
		}
	}

	@Override
	public List<MarkerSource> getMarkers() throws Exception {
		ArrayList<MarkerSource> markers = new ArrayList<MarkerSource>();
		markers.addAll(polygon.getPolygonPoints());
		return markers;
	}

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionSurveyFragment();
		fragment.setItem(this);
		return fragment;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		try {
			List<msg_mission_item> list = new ArrayList<msg_mission_item>();
			build();
			for (LatLng point : grid.gridPoints) {
				msg_mission_item mavMsg = packSurveyPoint(point);
				list.add(mavMsg);
			}
			return list;
		} catch (Exception e) {
			return new ArrayList<msg_mission_item>();
		}
	}

	private msg_mission_item packSurveyPoint(LatLng point) {
		msg_mission_item mavMsg = new msg_mission_item();
		mavMsg.autocontinue = 1;
		mavMsg.target_component = 1;
		mavMsg.target_system = 1;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
		mavMsg.x = (float) point.latitude;
		mavMsg.y = (float) point.longitude;
		mavMsg.z = (float) surveyData.getAltitude().valueInMeters();
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

}
