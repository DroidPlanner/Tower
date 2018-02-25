package com.o3dr.services.android.lib.drone.mission.item.complex;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 11/7/14.
 */
public class SurveyDetail implements Parcelable {

    private double altitude;
    private double angle;
    private double overlap;
    private double sidelap;
    private boolean lockOrientation;
    private CameraDetail cameraDetail;

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getOverlap() {
        return overlap;
    }

    public void setOverlap(double overlap) {
        this.overlap = overlap;
    }

    public double getSidelap() {
        return sidelap;
    }

    public void setSidelap(double sidelap) {
        this.sidelap = sidelap;
    }

    public CameraDetail getCameraDetail() {
        return cameraDetail;
    }

    public void setCameraDetail(CameraDetail cameraDetail) {
        this.cameraDetail = cameraDetail;
    }

    /**
     * True if aircraft's yaw is locked to the angle of the survey
     * @since 3.0.0
     */
    public boolean getLockOrientation(){
        return lockOrientation;
    }

    /**
     * Lock aircraft's yaw to the angle of the survey
     * @param lockOrientation
     * @since 3.0.0
     */
    public void setLockOrientation(boolean lockOrientation){
        this.lockOrientation = lockOrientation;
    }

    public double getLateralFootPrint() {
        return altitude * cameraDetail.getSensorLateralSize()
                / cameraDetail.getFocalLength();

    }

    public double getLongitudinalFootPrint() {
        return altitude * cameraDetail.getSensorLongitudinalSize()
                / cameraDetail.getFocalLength();
    }

    public double getGroundResolution() {
        return ((altitude * cameraDetail.getSensorLateralSize() / cameraDetail.getFocalLength()
                        * (altitude * cameraDetail.getSensorLongitudinalSize()
                /  cameraDetail.getFocalLength()) / (cameraDetail.getSensorResolution() * 1000)))
                / 10000;
    }

    public double getLongitudinalPictureDistance() {
        return getLongitudinalFootPrint() * (1 - overlap * .01);
    }

    public double getLateralPictureDistance() {
        return getLateralFootPrint() * (1 - sidelap * .01);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.altitude);
        dest.writeDouble(this.angle);
        dest.writeDouble(this.overlap);
        dest.writeDouble(this.sidelap);
        dest.writeParcelable(this.cameraDetail, 0);
        dest.writeByte((byte)(this.lockOrientation?1:0));
    }

    public SurveyDetail() {
    }

    public SurveyDetail(SurveyDetail copy){
        this.altitude = copy.altitude;
        this.angle = copy.angle;
        this.overlap = copy.overlap;
        this.sidelap = copy.sidelap;
        this.lockOrientation = copy.lockOrientation;
        this.cameraDetail = copy.cameraDetail == null ? null : new CameraDetail(copy.cameraDetail);
    }

    private SurveyDetail(Parcel in) {
        this.altitude = in.readDouble();
        this.angle = in.readDouble();
        this.overlap = in.readDouble();
        this.sidelap = in.readDouble();
        this.cameraDetail = in.readParcelable(CameraDetail.class.getClassLoader());
        this.lockOrientation = in.readByte() != 0;
    }

    public static final Parcelable.Creator<SurveyDetail> CREATOR = new Parcelable.Creator<SurveyDetail>() {
        public SurveyDetail createFromParcel(Parcel source) {
            return new SurveyDetail(source);
        }

        public SurveyDetail[] newArray(int size) {
            return new SurveyDetail[size];
        }
    };
}
