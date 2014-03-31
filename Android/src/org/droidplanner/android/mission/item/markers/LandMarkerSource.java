package org.droidplanner.android.mission.item.markers;

import org.droidplanner.R;
import org.droidplanner.android.mission.item.MissionItemRender;

/**
 * This implements the marker source for the land mission item.
 */
class LandMarkerSource extends MissionItemMarkerSource {
    protected LandMarkerSource(MissionItemRender origin) {
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
