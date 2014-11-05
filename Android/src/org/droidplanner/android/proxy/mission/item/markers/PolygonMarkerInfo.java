package org.droidplanner.android.proxy.mission.item.markers;

import com.ox3dr.services.android.lib.coordinate.LatLong;

import org.droidplanner.android.maps.MarkerInfo;

/**
 */
public class PolygonMarkerInfo extends MarkerInfo.SimpleMarkerInfo {

	private final LatLong mPoint;

	public PolygonMarkerInfo(LatLong point) {
		mPoint = point;
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
	public com.ox3dr.services.android.lib.coordinate.LatLong getPosition() {
		return mPoint;
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
