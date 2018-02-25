package org.droidplanner.services.android.impl.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.services.android.impl.core.MAVLink.command.doCmd.MavLinkDoCmds;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.manager.MavLinkDroneManager;
import org.droidplanner.services.android.impl.core.gcs.roi.ROIEstimator;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by fhuya on 1/9/15.
 */
public class FollowGuidedScan extends FollowAbove {

    private static final long TIMEOUT = 1000; //ms

    public static final String EXTRA_FOLLOW_ROI_TARGET = "extra_follow_roi_target";

    public static final double DEFAULT_FOLLOW_ROI_ALTITUDE = 10; //meters
    private static final double sDefaultRoiAltitude = (DEFAULT_FOLLOW_ROI_ALTITUDE);

    @Override
    public FollowModes getType() {
        return FollowModes.GUIDED_SCAN;
    }

    public FollowGuidedScan(MavLinkDroneManager droneMgr, Handler handler) {
        super(droneMgr, handler);
    }

    @Override
    public void updateAlgorithmParams(Map<String, ?> params) {
        super.updateAlgorithmParams(params);

        final LatLongAlt target;

        LatLong tempCoord = (LatLong) params.get(EXTRA_FOLLOW_ROI_TARGET);
        if (tempCoord == null || tempCoord instanceof LatLongAlt) {
            target = (LatLongAlt) tempCoord;
        } else {
            target = new LatLongAlt(tempCoord, sDefaultRoiAltitude);
        }

        getROIEstimator().updateROITarget(target);
    }

    @Override
    protected ROIEstimator initROIEstimator(MavLinkDrone drone, Handler handler) {
        return new GuidedROIEstimator(drone, handler);
    }

    @Override
    public Map<String, Object> getParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(EXTRA_FOLLOW_ROI_TARGET, getROIEstimator().roiTarget);
        return params;
    }

    @Override
    protected GuidedROIEstimator getROIEstimator() {
        return (GuidedROIEstimator) super.getROIEstimator();
    }

    private static class GuidedROIEstimator extends ROIEstimator {

        private LatLongAlt roiTarget;

        public GuidedROIEstimator(MavLinkDrone drone, Handler handler) {
            super(drone, handler);
        }

        void updateROITarget(LatLongAlt roiTarget) {
            this.roiTarget = roiTarget;
            onLocationUpdate(null);
        }

        @Override
        protected void updateROI() {
            if (roiTarget == null) {
                System.out.println("Cancelling ROI lock.");
                //Fallback to the default behavior
                super.updateROI();
            } else {
                Timber.d("ROI Target: " + roiTarget.toString());

                //Track the target until told otherwise.
                MavLinkDoCmds.setROI(drone, roiTarget, null);
                watchdog.postDelayed(watchdogCallback, TIMEOUT);
            }
        }
    }
}
