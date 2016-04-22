package org.droidplanner.android.utils.ar.rendering;

import android.opengl.Matrix;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import org.droidplanner.android.utils.ar.ARWorld;
import org.droidplanner.android.utils.ar.telemetry.TelemetryUtils;
import org.droidplanner.android.utils.ar.telemetry.Vector3;

/**
 * Base class for AR OpenGL object.
 */
public class ARObject {
    private float yaw;
    private float pitch;
    private float roll;
    protected LatLongAlt location;

    public ARObject(float yaw, float pitch, float roll) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    public ARObject(LatLongAlt location) {
        this.yaw = 0.0f;    // TODO: Change this default?
        this.pitch = 0.0f;  // TODO: Change this default?
        this.roll = 0.0f;   // TODO: Change this default?
        this.location = location;
    }

    public float[] getModelMatrix() {
        float[] modelMatrix = new float[16];

        Vector3 vector = TelemetryUtils.toCameraVec3(ARWorld.getInstance().getHomeLocation(), location);
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, vector.getX(), vector.getY(), vector.getZ());

        return modelMatrix;
    }

    public void draw(float[] viewProjectionMatrix) {  }

    // TODO: create distance from tap method.
}
