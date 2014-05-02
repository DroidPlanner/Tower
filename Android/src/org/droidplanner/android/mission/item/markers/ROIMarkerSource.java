package org.droidplanner.android.mission.item.markers;

import org.droidplanner.R;
import org.droidplanner.android.mission.item.MissionItemRender;

/**
 * This implements the marker source for the ROI mission item.
 */
class ROIMarkerSource extends MissionItemMarkerSource {
    protected ROIMarkerSource(MissionItemRender origin) {
        super(origin);
    }

    @Override
    protected int getSelectedIconResource() {
        return R.drawable.ic_wp_map_selected;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.ic_roi;
    }
}
