package org.droidplanner.android.mission.item.markers;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.android.graphic.map.MarkerManager.MarkerSource;
import org.droidplanner.android.mission.item.MissionItemRender;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.mission.survey.Survey;

public class MissionItemSurveyMarkerSource extends MissionItemGenericMarkerSource{
	
	private Survey survey;
	private List<MarkerSource> polygonMarkers = new ArrayList<MarkerSource>();

	public MissionItemSurveyMarkerSource(MissionItemRender origin) {
		super(origin);
		survey = (Survey) origin.getMissionItem();
		updateMarkerSourceList();
	}

	private void updateMarkerSourceList() {
		for (Coord2D point : survey.polygon.getPoints()) {
			polygonMarkers.add(new PolygonMarkerSource(point));
		}
	}

	public List<MarkerSource> getMarkers() {
		return polygonMarkers;
	}	
	
}
