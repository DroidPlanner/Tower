package org.droidplanner.android.proxy.mission.item.markers;

import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.mission.survey.Survey2D;

/**
 */
public class PolygonMarkerInfo extends MarkerInfo.SimpleMarkerInfo {

	private Coord2D mPoint;
	private Survey2D survey;
	private int polygonIndex;

	public PolygonMarkerInfo(Coord2D point, Survey2D mSurvey, int index) {
		mPoint = point;
		survey = mSurvey;
		polygonIndex = index;
	}
	
	public Survey2D getSurvey(){
		return survey;
	}

	public int getIndex(){
		return polygonIndex;
	}

	
	@Override
	public float getAnchorU() {
		return 0.5f;
	}

	@Override
	public float getAnchorV() {
		return 0.5f;
	}

	@Override
	public Coord2D getPosition() {
		return mPoint;
	}

	@Override
	public void setPosition(Coord2D coord) {
		mPoint = coord;
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isFlat() {
		return true;
	}
}
