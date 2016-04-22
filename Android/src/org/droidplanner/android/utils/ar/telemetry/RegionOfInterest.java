package org.droidplanner.android.utils.ar.telemetry;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

/**
 * Functionality related to calculating ROI.
 */
public class RegionOfInterest {
    private static float ROI_DEFAULT_DISTANCE = 100.0f; // If our gimbal isn't pointing down at all,
                                                        // use this distance as our focal point

    // Given Solo’s current attitude and gimbal attitude, return the location of the ROI
    // Assumptions:
    // 1. Copter is level to the ground.
    // 2. Altitude is accurate.
    // 3. Ground is flat.
    public static LatLongAlt computeROI(LatLongAlt vehicleLocation, float heading, float pitch) {
        // Just guess at what our ROI is based on where we're pointing.
        LatLongAlt roi;

        //In these special cases, return a point straight ahead of the copter
        // 1. No gimbal pitch.
        // 2. Copter altitude is below 0.0 (can't intersect with the ground plane in this case).
        if (pitch == 0.0 || vehicleLocation.getAltitude() <= 0.0) {
            // Pick a point at a default distance in that direction
            roi = TelemetryUtils.newLocationFromAzimuthAndDistance(vehicleLocation, heading, ROI_DEFAULT_DISTANCE);
        } else {
            // Find intersection with ground planebased on altitude and gimbal pitch, we intersect
            // the ground at this distance.
            float inversePitch = 90 - pitch;
            float dist = (float) (vehicleLocation.getAltitude() * Math.tan(Math.toRadians((double) inversePitch)));
            roi = TelemetryUtils.newLocationFromAzimuthAndDistance(vehicleLocation, heading, dist);
            roi.setAltitude(0.0);
        }

        return roi;
    }
}
