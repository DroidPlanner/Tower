package org.droidplanner.android.proxy.mission.item.markers;

import org.droidplanner.R;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;

/**
 * This implements the marker source for the land mission item.
 */
class LandMarkerInfo extends MissionItemMarkerInfo {
    protected LandMarkerInfo(MissionItemProxy origin) {
        super(origin);
    }

    @Override
    protected int getSelectedIconResource() {
        return R.drawable.ic_wp_lan_selected;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.ic_wp_land;
    }
}
