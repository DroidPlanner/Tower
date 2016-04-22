package org.droidplanner.android.utils.ar.telemetry;

import org.droidplanner.android.utils.ar.rendering.ARCamera;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Syncs telemetry to video frames so that OpenGL objects are drawn at the right position in video
 * relative to the vehicle.
 */
public class ARTelemetryToVideoSyncer {
    private final static int TELEMETRY_QUEUE_SIZE = 50;

    private LinkedBlockingDeque<VehicleTelemetry> telemetryDataQueue;

    public ARTelemetryToVideoSyncer() {
        telemetryDataQueue = new LinkedBlockingDeque<>(TELEMETRY_QUEUE_SIZE);
    }

    public void bufferTelemetryData(VehicleTelemetry vehicleTelemetry) {
        if (telemetryDataQueue.size() == TELEMETRY_QUEUE_SIZE) {
            telemetryDataQueue.remove();
        }

        telemetryDataQueue.add(vehicleTelemetry);
    }

    public void clearBufferedTelemetryData() {
        telemetryDataQueue.clear();
    }

    public void computeARCameraProjectionMatrix(long videoFrameTimestamp) {
        VehicleTelemetry vehicleTelemetry = null;

        // Get telemetry from buffer corresponding to the timestamp of the video frame.
        // TODO: improve this algorithm.
        for (VehicleTelemetry telemetry : telemetryDataQueue) {
            if (telemetry.getTimeStamp() < videoFrameTimestamp) {
                continue;
            } else {
                vehicleTelemetry = telemetry;
                break;
            }
        }

        if (vehicleTelemetry == null) {
            vehicleTelemetry = telemetryDataQueue.getLast();
        }

        ARCamera.getInstance().updateViewProjectionMatrix(vehicleTelemetry);
    }
}
