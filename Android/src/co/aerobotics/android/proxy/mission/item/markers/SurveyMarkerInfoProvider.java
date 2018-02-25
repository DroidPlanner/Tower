package co.aerobotics.android.proxy.mission.item.markers;

import co.aerobotics.android.maps.MarkerInfo;
import co.aerobotics.android.proxy.mission.item.MissionItemProxy;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SurveyMarkerInfoProvider {

	private final Survey mSurvey;
	protected final MissionItemProxy markerOrigin;
	private final List<MarkerInfo> mPolygonMarkers = new ArrayList<MarkerInfo>();

	protected SurveyMarkerInfoProvider(MissionItemProxy origin) {
		this.markerOrigin = origin;
		mSurvey = (Survey) origin.getMissionItem();
		updateMarkerInfoList();
	}

	private void updateMarkerInfoList() {
        List<LatLong> points = mSurvey.getPolygonPoints();
		List<LatLong> gridPoints = mSurvey.getGridPoints();
        if(points != null) {
            final int pointsCount = points.size();
            for (int i = 0; i < pointsCount; i++) {
                mPolygonMarkers.add(new PolygonMarkerInfo(points.get(i), markerOrigin, mSurvey, i));
            }
        }
        /*if (gridPoints != null && gridPoints.size() != 0) {
			mPolygonMarkers.add(new LastWaypointMarkerInfo(gridPoints.get(gridPoints.size() - 1)));
		}*/
	}

	public List<MarkerInfo> getMarkersInfos() {
		return mPolygonMarkers;
	}

	public MissionItemProxy getMarkerOrigin() {
		return markerOrigin;
	}
}
