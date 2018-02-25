package org.droidplanner.services.android.impl.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.manager.MavLinkDroneManager;
import org.droidplanner.services.android.impl.core.gcs.location.Location;
import org.droidplanner.services.android.impl.core.gcs.roi.ROIEstimator;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class FollowAlgorithm {

    protected final MavLinkDroneManager droneMgr;
    private final ROIEstimator roiEstimator;
    private final AtomicBoolean isFollowEnabled = new AtomicBoolean(false);

    public FollowAlgorithm(MavLinkDroneManager droneMgr, Handler handler) {
        this.droneMgr = droneMgr;

        final MavLinkDrone drone = droneMgr.getDrone();
        this.roiEstimator = initROIEstimator(drone, handler);
    }

    protected boolean isFollowEnabled() {
        return isFollowEnabled.get();
    }

    public void enableFollow() {
        isFollowEnabled.set(true);
        if(roiEstimator != null)
            roiEstimator.enableFollow();
    }

    public void disableFollow() {
        if (isFollowEnabled.compareAndSet(true, false)) {
            if (roiEstimator != null)
                roiEstimator.disableFollow();
        }
    }

    public void updateAlgorithmParams(Map<String, ?> paramsMap) {
    }

    protected ROIEstimator initROIEstimator(MavLinkDrone drone, Handler handler) {
        return new ROIEstimator(drone, handler);
    }

    protected ROIEstimator getROIEstimator() {
        return roiEstimator;
    }

    public final void onLocationReceived(Location location) {
        if (isFollowEnabled.get()) {
            if(roiEstimator != null)
                roiEstimator.onLocationUpdate(location);
            processNewLocation(location);
        }
    }

    protected abstract void processNewLocation(Location location);

    public abstract FollowModes getType();

    public Map<String, Object> getParams() {
        return Collections.emptyMap();
    }

    public enum FollowModes {
        LEASH("Leash"),
        LEAD("Lead"),
        RIGHT("Right"),
        LEFT("Left"),
        CIRCLE("Orbit"),
        ABOVE("Above"),
        SPLINE_LEASH("Vector Leash"),
        SPLINE_ABOVE("Vector Above"),
        GUIDED_SCAN("Guided Scan"),
        LOOK_AT_ME("Look At Me"),
        SOLO_SHOT("Solo Follow Shot");

        private String name;

        FollowModes(String str) {
            name = str;
        }

        @Override
        public String toString() {
            return name;
        }

        public FollowModes next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public FollowAlgorithm getAlgorithmType(MavLinkDroneManager droneMgr, Handler handler) {
            switch (this) {
                case LEASH:
                default:
                    return new FollowLeash(droneMgr, handler, 8.0);
                case LEAD:
                    return new FollowLead(droneMgr, handler, 15.0);
                case RIGHT:
                    return new FollowRight(droneMgr, handler, 10.0);
                case LEFT:
                    return new FollowLeft(droneMgr, handler, 10.0);
                case CIRCLE:
                    return new FollowCircle(droneMgr, handler, 15.0, 10.0);
                case ABOVE:
                    return new FollowAbove(droneMgr, handler);
                case SPLINE_LEASH:
                    return new FollowSplineLeash(droneMgr, handler, 8.0);
                case SPLINE_ABOVE:
                    return new FollowSplineAbove(droneMgr, handler);
                case GUIDED_SCAN:
                    return new FollowGuidedScan(droneMgr, handler);
                case LOOK_AT_ME:
                    return new FollowLookAtMe(droneMgr, handler);
                case SOLO_SHOT:
                    return new FollowSoloShot(droneMgr, handler);
            }
        }
    }

}
