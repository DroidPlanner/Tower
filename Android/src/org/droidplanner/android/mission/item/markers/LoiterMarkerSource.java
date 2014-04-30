package org.droidplanner.android.mission.item.markers;

import org.droidplanner.R;
import org.droidplanner.android.mission.item.MissionItemRender;

/**
 * This implements the marker source for the loiter mission item.
 */
class LoiterMarkerSource extends MissionItemMarkerSource {
    protected LoiterMarkerSource(MissionItemRender origin) {
        super(origin);
    }

    @Override
    protected int getSelectedIconResource() {
        return R.drawable.ic_wp_loiter_selected;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.ic_wp_loiter;
    }
}
