package org.droidplanner.android.proxy.mission.item.markers;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;

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
        List<LatLong> points = mSurvey.getPolygonPoints();
        if(points != null) {
            final int pointsCount = points.size();
            for (int i = 0; i < pointsCount; i++) {
                mPolygonMarkers.add(new PolygonMarkerInfo(points.get(i), mSurvey, i));
            }
        }
	}

	public List<MarkerInfo> getMarkersInfos() {
		return mPolygonMarkers;
	}
}
