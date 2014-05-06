package org.droidplanner.android.proxy.mission.item.markers;

import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.mission.survey.Survey;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SurveyMarkerInfo extends MissionItemMarkerInfo {

    private final Survey mSurvey;
    private final List<MarkerInfo> mPolygonMarkers = new ArrayList<MarkerInfo>();

    protected SurveyMarkerInfo(MissionItemProxy origin) {
        super(origin);
        mSurvey = (Survey) origin.getMissionItem();
        updateMarkerInfoList();
    }

    private void updateMarkerInfoList(){
        for(Coord2D point: mSurvey.polygon.getPoints()){
            mPolygonMarkers.add(new PolygonMarkerInfo(point));
        }
    }

    public List<MarkerInfo> getMarkersInfos(){
        return mPolygonMarkers;
    }

    @Override
    protected int getSelectedIconResource() {
        return 0;
    }

    @Override
    protected int getIconResource() {
        return 0;
    }
}
