package com.droidplanner.drone.variables.mission.survey;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.mission.MissionDetailFragment;
import com.droidplanner.fragments.mission.MissionSurveyFragment;
import com.droidplanner.polygon.Polygon;
import com.google.android.gms.maps.model.LatLng;

public class Survey extends MissionItem {

	private Polygon polygon = new Polygon();

	public Survey(MissionItem item) {
	}

	@Override
	public List<LatLng> getPath() throws Exception {
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

}
