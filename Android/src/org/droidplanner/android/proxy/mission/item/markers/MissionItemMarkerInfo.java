package org.droidplanner.android.proxy.mission.item.markers;

import android.content.res.Resources;
import android.graphics.Bitmap;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.maps.MarkerWithText;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Template class and factory for a mission item's marker source.
 */
public abstract class MissionItemMarkerInfo extends MarkerInfo {

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

		case REGION_OF_INTEREST:
			markerInfos.add(new ROIMarkerInfo(origin));
			break;

		case WAYPOINT:
			markerInfos.add(new WaypointMarkerInfo(origin));
			break;

		case SPLINE_WAYPOINT:
			markerInfos.add(new SplineWaypointMarkerInfo(origin));
			break;

		case STRUCTURE_SCANNER:
			markerInfos.add(new StructureScannerMarkerInfoProvider(origin));
			break;

		case SPLINE_SURVEY:
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
	public com.o3dr.services.android.lib.coordinate.LatLong getPosition() {
		return ((MissionItem.SpatialItem) mMarkerOrigin.getMissionItem()).getCoordinate();
	}

	@Override
	public void setPosition(LatLong coord) {
		LatLong coordinate = ((MissionItem.SpatialItem) mMarkerOrigin.getMissionItem())
                .getCoordinate();
        coordinate.setLatitude(coord.getLatitude());
        coordinate.setLongitude(coord.getLongitude());
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
