package org.droidplanner.android.utils.ar;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import org.droidplanner.android.utils.ar.rendering.ARObject;
import org.droidplanner.android.utils.ar.telemetry.ARTelemetryToVideoSyncer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import timber.log.Timber;

/**
 * Initializes objects used throughout the AR framework.
 */
public class ARWorld {
    private ARTelemetryToVideoSyncer telemetryToVideoSyncer;
    private List<ARObject> objectsToDraw;

    private LatLongAlt vehicleLocation;
    private LatLongAlt homeLocation;

    private static ARWorld instance;

    public static synchronized ARWorld getInstance() {
        if (instance == null) {
            instance = new ARWorld();
        }

        return instance;
    }

    public ARWorld() {
        telemetryToVideoSyncer = new ARTelemetryToVideoSyncer();
        objectsToDraw = new CopyOnWriteArrayList<>();

        vehicleLocation = new LatLongAlt(0.0, 0.0, 0.0);
        homeLocation = new LatLongAlt(0.0, 0.0, 0.0);
    }

    public List<ARObject> getObjectsToDraw() {
        // TODO: this should be queried by ARRenderer to draw in onDraw method.
        return objectsToDraw;
    }

    public void addObjectToDraw(ARObject arObject) {
        objectsToDraw.add(arObject);
    }

    public ARTelemetryToVideoSyncer getTelemetryToVideoSyncer() {
        return telemetryToVideoSyncer;
    }

    public LatLongAlt getVehicleLocation() {
        return vehicleLocation;
    }

    public void setVehicleLocation(LatLongAlt vehicleLocation) {
        this.vehicleLocation = vehicleLocation;
    }

    public LatLongAlt getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(LatLongAlt homeLocation) {
        this.homeLocation = homeLocation;
    }

    public void printMatrix(float[] matrix) {
        for (int i = 0; i < 4; ++i) {
            Timber.d(" " + matrix[0 + i] +
                " " + matrix[1 + i] +
                " " + matrix[2 + i] +
                " " + matrix[3 + i] + "\n");
        }
    }
}
