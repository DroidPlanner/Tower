package co.aerobotics.android.proxy.mission.item.markers;

import co.aerobotics.android.proxy.mission.item.MissionItemProxy;

/**
 * This implements the marker source for a takeoff mission item.
 */
class TakeoffMarkerInfo extends MissionItemMarkerInfo {

	protected TakeoffMarkerInfo(MissionItemProxy origin) {
		super(origin);
	}

	@Override
	protected int getSelectedIconResource() {
		return co.aerobotics.android.R.drawable.ic_wp_takeof_selected;
	}

	@Override
	protected int getIconResource() {
		return co.aerobotics.android.R.drawable.ic_wp_takeof_selected;
	}
}
