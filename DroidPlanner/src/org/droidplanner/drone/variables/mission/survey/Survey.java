package org.droidplanner.drone.variables.mission.survey;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.drone.variables.mission.Mission;
import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.drone.variables.mission.survey.grid.Grid;
import org.droidplanner.drone.variables.mission.survey.grid.GridBuilder;
import org.droidplanner.file.IO.CameraInfoReader;
import org.droidplanner.file.help.CameraInfoLoader;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionSurveyFragment;
import org.droidplanner.helpers.units.Altitude;
import org.droidplanner.polygon.Polygon;

import android.content.Context;
import android.widget.Toast;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.google.android.gms.maps.model.LatLng;

public class Survey extends MissionItem {

	private Polygon polygon = new Polygon();
	private SurveyData surveyData = new SurveyData();
	private CameraInfoLoader avaliableCameras;
	private Context context;

	public Survey(Mission mission,List<LatLng> points, Context context) {
		super(mission);
		this.context = context;
		avaliableCameras = new CameraInfoLoader(context);
		polygon.addPoints(points);
		
		surveyData.setCameraInfo(CameraInfoReader.getNewMockCameraInfo());
	}

	@Override
	public List<LatLng> getPath() throws Exception {
		surveyData.update(0, new Altitude(50), 0, 0);
		
		try {
			GridBuilder gridBuilder = new GridBuilder(polygon, surveyData, new LatLng(0, 0),context);
			polygon.checkIfValid(context);
			Grid grid = gridBuilder.generate();
			grid.setAltitude(surveyData.getAltitude());
			return grid.getCameraLocations();
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		throw new Exception();
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
	public msg_mission_item packMissionItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		// TODO Auto-generated method stub
		
	}

}
