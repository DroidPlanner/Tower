package org.droidplanner.android.utils.prefs;

import org.droidplanner.R;

/**
 * Map auto pan target types.
 */
public enum AutoPanMode {
    USER(R.string.map_auto_pan_mode_user),
    DRONE(R.string.map_auto_pan_mode_drone),
    DISABLED(R.string.map_auto_pan_mode_disabled);

    private final int mResourceId;

    private AutoPanMode(int resourceId){
        mResourceId = resourceId;
    }

    public int getResourceId(){
        return mResourceId;
    }
}
