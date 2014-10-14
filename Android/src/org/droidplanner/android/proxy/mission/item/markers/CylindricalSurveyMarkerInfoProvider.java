package org.droidplanner.android.proxy.mission.item.markers;

import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.core.mission.survey.CylindricalSurvey;

/**
 *
 */
public class CylindricalSurveyMarkerInfoProvider extends MarkerInfo.SimpleMarkerInfo {


	private MarkerInfo.SimpleMarkerInfo marker;

	protected CylindricalSurveyMarkerInfoProvider(MissionItemProxy origin) {
		CylindricalSurvey survey = (CylindricalSurvey) origin.getMissionItem();
		marker = new PolygonMarkerInfo(survey.getCenter());
	}

	public MarkerInfo.SimpleMarkerInfo getMarkerInfo() {
		return marker;
	}
}
