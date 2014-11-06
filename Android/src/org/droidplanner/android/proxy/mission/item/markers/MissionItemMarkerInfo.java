package org.droidplanner.android.proxy.mission.item.markers;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.maps.MarkerWithText;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;

import android.content.res.Resources;
import android.graphics.Bitmap;

import com.ox3dr.services.android.lib.coordinate.LatLong;

/**
 * Template class and factory for a mission item's marker source.
 */
public abstract class MissionItemMarkerInfo extends MarkerInfo.SimpleMarkerInfo {

	protected final MissionItemProxy mMarkerOrigin;

	public static List<MarkerInfo> newInstance(MissionItemProxy origin) {
		List<MarkerInfo> markerInfos = new ArrayList<MarkerInfo>();
		switch (origin.getMissionItem().getType()) {
		case LAND:
			markerInfos.add(new LandMarkerInfo(origin));
			break;
		case CIRCLE:
			markerInfos.add(new LoiterMarkerInfo(origin));
			break;

		case ROI:
			markerInfos.add(new ROIMarkerInfo(origin));
			break;

		case WAYPOINT:
			markerInfos.add(new WaypointMarkerInfo(origin));
			break;

		case SPLINE_WAYPOINT:
			markerInfos.add(new SplineWaypointMarkerInfo(origin));
			break;

		case CYLINDRICAL_SURVEY:
			markerInfos.add(new StructureScannerMarkerInfoProvider(origin));
			break;
			
		case SURVEY:
			markerInfos.addAll(new SurveyMarkerInfoProvider(origin).getMarkersInfos());
			break;

		default:
			break;
		}

		return markerInfos;
	}

	protected MissionItemMarkerInfo(MissionItemProxy origin) {
		mMarkerOrigin = origin;
	}

	public MissionItemProxy getMarkerOrigin() {
		return mMarkerOrigin;
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
		return ((SpatialCoordItem) mMarkerOrigin.getMissionItem()).getCoordinate();
	}

	@Override
	public void setPosition(LatLong coord) {
		((SpatialCoordItem) mMarkerOrigin.getMissionItem()).setPosition(coord);
	}

	@Override
	public boolean isDraggable() {
		return true;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public Bitmap getIcon(Resources res) {
		int drawable;
		final MissionProxy missionProxy = mMarkerOrigin.getMissionProxy();
		if (missionProxy.selection.selectionContains(mMarkerOrigin)) {
			drawable = getSelectedIconResource();
		} else {
			drawable = getIconResource();
		}

		return MarkerWithText.getMarkerWithTextAndDetail(drawable,
				Integer.toString(missionProxy.getOrder(mMarkerOrigin)), getIconDetail(), res);
	}

	private String getIconDetail() {
		try {
			final MissionProxy missionProxy = mMarkerOrigin.getMissionProxy();
			if (missionProxy.getAltitudeDiffFromPreviousItem(mMarkerOrigin) == 0) {
				return null;
			} else {
				return null; // altitude.toString();
			}
		} catch (Exception e) {
			return null;
		}
	}

	protected abstract int getSelectedIconResource();

	protected abstract int getIconResource();
}
