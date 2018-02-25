package co.aerobotics.android.proxy.mission.item.markers;

import co.aerobotics.android.proxy.mission.item.MissionItemProxy;
import co.aerobotics.android.R;

/**
 *
 */
public class StructureScannerMarkerInfoProvider extends MissionItemMarkerInfo {

	protected StructureScannerMarkerInfoProvider(MissionItemProxy origin) {
		super(origin);
	}

	@Override
	protected int getSelectedIconResource() {
		return R.drawable.ic_wp_loiter_selected;
	}

	@Override
	protected int getIconResource() {
		return R.drawable.ic_wp_circle_ccw;
	}
}
