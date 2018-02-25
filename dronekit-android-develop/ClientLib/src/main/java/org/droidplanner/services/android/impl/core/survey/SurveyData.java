package org.droidplanner.services.android.impl.core.survey;

import org.droidplanner.services.android.impl.core.helpers.units.Area;

import java.util.Locale;

public class SurveyData {
    private CameraInfo camera = new CameraInfo();
    private double altitude;
    private Double angle;
    private Double overlap;
    private Double sidelap;
    private boolean lockOrientation;
    private Footprint footprint;

    public SurveyData() {
        update(0, (50.0), 50, 60, false);
    }

    public void update(double angle, double altitude, double overlap, double sidelap, boolean lockOrientation) {
        this.angle = angle;
        this.overlap = overlap;
        this.sidelap = sidelap;
        setAltitude(altitude);
        this.lockOrientation = lockOrientation;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
        this.footprint = new Footprint(camera, this.altitude);
    }

    public void setCameraInfo(CameraInfo info) {
        this.camera = info;
        this.footprint = new Footprint(this.camera, this.altitude);
        tryToLoadOverlapFromCamera();
    }

    public CameraInfo getCameraInfo() {
        return this.camera;
    }

    public void setLockOrientation(boolean lockOrientation) {
        this.lockOrientation = lockOrientation;
    }

    private void tryToLoadOverlapFromCamera() {
        if (camera.overlap != null) {
            this.overlap = camera.overlap;
        }
        if (camera.sidelap != null) {
            this.sidelap = camera.sidelap;
        }
    }

    public double getLongitudinalPictureDistance() {
        return getLongitudinalFootPrint() * (1 - overlap * .01);
    }

    public double getLateralPictureDistance() {
        return getLateralFootPrint() * (1 - sidelap * .01);
    }

    public double getAltitude() {
        return altitude;
    }

    public Double getAngle() {
        return angle;
    }

    public double getSidelap() {
        return sidelap;
    }

    public double getOverlap() {
        return overlap;
    }

    public double getLateralFootPrint() {
        return footprint.getLateralSize();
    }

    public double getLongitudinalFootPrint() {
        return footprint.getLongitudinalSize();
    }

    public Area getGroundResolution() {
        return new Area(footprint.getGSD() * 0.01);
    }

    public String getCameraName() {
        return camera.name;
    }

    public boolean getLockOrientation(){
        return lockOrientation;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Altitude: %f Angle %f Overlap: %f Sidelap: %f Locked Orientation: %b", altitude,
                angle, overlap, sidelap, lockOrientation);
    }

}