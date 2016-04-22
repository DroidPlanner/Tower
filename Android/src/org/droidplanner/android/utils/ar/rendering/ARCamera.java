package org.droidplanner.android.utils.ar.rendering;

import android.opengl.Matrix;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import org.droidplanner.android.utils.ar.ARWorld;
import org.droidplanner.android.utils.ar.telemetry.RegionOfInterest;
import org.droidplanner.android.utils.ar.telemetry.TelemetryUtils;
import org.droidplanner.android.utils.ar.telemetry.Vector3;
import org.droidplanner.android.utils.ar.telemetry.VehicleTelemetry;

import timber.log.Timber;

/**
 * Abstracts vehicle camera for drawing AR OpenGL objects.
 */
public class ARCamera {
    private static float FIELD_OF_VIEW_IN_Y_DIRECTION = 55.0f;

    private float[] projectionMatrix = new float[16];
    private float[] viewProjectionMatrix = new float[16];

    private static ARCamera instance;

    public static synchronized ARCamera getInstance() {
        if (instance == null) {
            instance = new ARCamera();
        }

        return instance;
    }

    private ARCamera() {
        init();
    }

    public void init() {
        Matrix.perspectiveM(projectionMatrix, 0, FIELD_OF_VIEW_IN_Y_DIRECTION, 16.0f / 9.0f, 0.1f, 1000.0f);
    }

    public void updateViewProjectionMatrix(VehicleTelemetry vehicleTelemetry) {
        Vector3 locationCoord = TelemetryUtils.toCameraVec3(vehicleTelemetry.getHomeLocation(),
            vehicleTelemetry.getVehicleLocation());

        LatLongAlt roi = RegionOfInterest.computeROI(vehicleTelemetry.getVehicleLocation(),
            (float) vehicleTelemetry.getGimbalYaw(), (float) vehicleTelemetry.getGimbalPitch());

        Vector3 attitudeCoord = TelemetryUtils.toCameraVec3(vehicleTelemetry.getHomeLocation(), roi);

        float[] viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0, locationCoord.getX(), locationCoord.getY(), locationCoord.getZ(),
            attitudeCoord.getX(), attitudeCoord.getY(), attitudeCoord.getZ(), 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Timber.d("Updated telemetry data in telemetry-to-video syncer: " + vehicleTelemetry.toString());
        Timber.d("\nComputed AR object view projection matrix from telemetry: \n\n");
        ARWorld.getInstance().printMatrix(viewProjectionMatrix);
    }

    public float[] getViewProjectionMatrix() {
        return viewProjectionMatrix;
    }
}
