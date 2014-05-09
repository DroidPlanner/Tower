package org.droidplanner.android.mission.item.markers;

import org.droidplanner.R;
import org.droidplanner.android.mission.item.MissionItemRender;

/**
 * This implements the marker source for a takeoff mission item.
 */
class TakeoffMarkerSource extends MissionItemMarkerSource {

    protected TakeoffMarkerSource(MissionItemRender origin) {
        super(origin);
    }

    @Override
    protected int getSelectedIconResource() {
        return R.drawable.ic_wp_takeof_selected;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.ic_wp_takeof_selected;
    }
}
