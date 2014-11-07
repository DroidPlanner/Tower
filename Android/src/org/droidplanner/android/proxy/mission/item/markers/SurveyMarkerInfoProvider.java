package org.droidplanner.android.proxy.mission.item.markers;

import com.ox3dr.services.android.lib.coordinate.LatLong;
import com.ox3dr.services.android.lib.drone.mission.item.complex.Survey;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;

/**
 *
 */
public class SurveyMarkerInfoProvider {

	private final Survey mSurvey;
	private final List<MarkerInfo> mPolygonMarkers = new ArrayList<MarkerInfo>();

	protected SurveyMarkerInfoProvider(MissionItemProxy origin) {
		mSurvey = (Survey) origin.getMissionItem();
		updateMarkerInfoList();
	}

	private void updateMarkerInfoList() {
		for (LatLong point : mSurvey.polygon.getPoints()) {
			mPolygonMarkers.add(new PolygonMarkerInfo(point));
		}
	}

	public List<MarkerInfo> getMarkersInfos() {
		return mPolygonMarkers;
	}
}
