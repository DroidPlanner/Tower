package org.droidplanner.services.android.impl.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.services.android.impl.core.drone.manager.MavLinkDroneManager;

public class FollowLeft extends FollowHeadingAngle {

    public FollowLeft(MavLinkDroneManager droneMgr, Handler handler, double radius) {
        super(droneMgr, handler, radius, -90.0);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.LEFT;
    }

}
