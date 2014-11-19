package org.droidplanner.android.proxy.mission.item.markers;

import org.droidplanner.android.R;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;

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
