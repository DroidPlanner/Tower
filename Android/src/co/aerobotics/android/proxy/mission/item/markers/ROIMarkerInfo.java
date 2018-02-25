package co.aerobotics.android.proxy.mission.item.markers;

import co.aerobotics.android.proxy.mission.item.MissionItemProxy;

/**
 * This implements the marker source for the ROI mission item.
 */
class ROIMarkerInfo extends MissionItemMarkerInfo {
	protected ROIMarkerInfo(MissionItemProxy origin) {
		super(origin);
	}

	@Override
	protected int getSelectedIconResource() {
		return co.aerobotics.android.R.drawable.ic_wp_map_selected;
	}

	@Override
	protected int getIconResource() {
		return co.aerobotics.android.R.drawable.ic_roi;
	}
}
