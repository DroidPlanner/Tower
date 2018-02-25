package co.aerobotics.android.proxy.mission.item.markers;

import co.aerobotics.android.proxy.mission.item.MissionItemProxy;

/**
 * This implements the marker source for the loiter mission item.
 */
class LoiterMarkerInfo extends MissionItemMarkerInfo {
	protected LoiterMarkerInfo(MissionItemProxy origin) {
		super(origin);
	}

	@Override
	protected int getSelectedIconResource() {
		return co.aerobotics.android.R.drawable.ic_wp_loiter_selected;
	}

	@Override
	protected int getIconResource() {
		return co.aerobotics.android.R.drawable.ic_wp_circle_cw;
	}
}
