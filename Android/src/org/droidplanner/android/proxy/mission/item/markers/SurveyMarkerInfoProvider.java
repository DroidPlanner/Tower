package org.droidplanner.android.proxy.mission.item.markers;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.mission.survey.Survey2D;

/**
 *
 */
public class SurveyMarkerInfoProvider {

	private final Survey2D mSurvey;
	private final List<MarkerInfo> mPolygonMarkers = new ArrayList<MarkerInfo>();

	protected SurveyMarkerInfoProvider(MissionItemProxy origin) {
		mSurvey = (Survey2D) origin.getMissionItem();
		updateMarkerInfoList();
	}

	private void updateMarkerInfoList() {
		List<Coord2D> points = mSurvey.polygon.getPoints();
		for (Coord2D point : points) {
			mPolygonMarkers.add(new PolygonMarkerInfo(point,mSurvey, points.indexOf(point)));
		}
	}

	public List<MarkerInfo> getMarkersInfos() {
		return mPolygonMarkers;
	}
}
