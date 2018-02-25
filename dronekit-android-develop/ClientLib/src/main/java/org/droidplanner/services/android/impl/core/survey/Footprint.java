package org.droidplanner.services.android.impl.core.survey;

import com.MAVLink.ardupilotmega.msg_camera_feedback;

import org.droidplanner.services.android.impl.core.helpers.geoTools.GeoTools;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.util.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class Footprint {
    /**
     * Vertex of the footprint in local frame index 0 is top right, where top is
     * direction of longitudinal travel. Index increases CCW
     */
    private final List<LatLong> vertex = new ArrayList<>();
    private double meanGSD;

    public Footprint(CameraInfo camera, double altitude) {
        this(camera, new LatLong(0, 0), (float) altitude, 0, 0, 0);
    }

    public Footprint(CameraInfo camera, msg_camera_feedback msg) {
        this(camera, new LatLong(msg.lat / 1E7, msg.lng / 1E7), msg.alt_rel, msg.pitch, msg.roll, msg.yaw);
    }

    public Footprint(CameraInfo camera, LatLong center, double alt, double pitch, double roll, double yaw) {
        double sx = camera.getSensorLateralSize() / 2;
        double sy = camera.getSensorLongitudinalSize() / 2;
        double f = camera.focalLength;
        double[][] dcm = MathUtils.dcmFromEuler(Math.toRadians(pitch), Math.toRadians(-roll + 180), Math.toRadians(-yaw));
        vertex.add(cameraFrameToLocalFrame(new LatLong(-sx, -sy), dcm, alt, f, center));
        vertex.add(cameraFrameToLocalFrame(new LatLong(+sx, -sy), dcm, alt, f, center));
        vertex.add(cameraFrameToLocalFrame(new LatLong(+sx, +sy), dcm, alt, f, center));
        vertex.add(cameraFrameToLocalFrame(new LatLong(-sx, +sy), dcm, alt, f, center));

        meanGSD = 0.001 * getLateralSize() * (sy / sx)
                / Math.sqrt(camera.sensorResolution);
    }

    /**
     * based on http://www.asprs.org/a/publications/pers/2005journal/july/2005_july_863-871.pdf
     */
    static private LatLong cameraFrameToLocalFrame(LatLong img, double[][] dcm, double alt,
                                                   double focalLength, LatLong center) {
        double x = alt
                * (dcm[0][0] * img.getLatitude() + dcm[1][0] * img.getLongitude() + dcm[2][0] * (-focalLength))
                / (dcm[0][2] * img.getLatitude() + dcm[1][2] * img.getLongitude() + dcm[2][2] * (-focalLength));
        double y = alt
                * (dcm[0][1] * img.getLatitude() + dcm[1][1] * img.getLongitude() + dcm[2][1] * (-focalLength))
                / (dcm[0][2] * img.getLatitude() + dcm[1][2] * img.getLongitude() + dcm[2][2] * (-focalLength));

        return GeoTools.moveCoordinate(center, x, y);
    }

    public double getLateralSize() {
        return (GeoTools.getDistance(vertex.get(0), vertex.get(1)) + GeoTools
                .getDistance(vertex.get(2), vertex.get(3))) / 2;
    }

    public double getLongitudinalSize() {
        return (GeoTools.getDistance(vertex.get(0), vertex.get(3)) + GeoTools
                .getDistance(vertex.get(1), vertex.get(2))) / 2;
    }

    public List<LatLong> getVertexInGlobalFrame() {
        return vertex;
    }

    public double getGSD() {
        return meanGSD;
    }
}
