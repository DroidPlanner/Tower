package com.o3dr.services.android.lib.drone.mission.item.complex;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 11/6/14.
 */
public class CameraDetail implements Parcelable {

    private final String name;
    private final double sensorWidth;
    private final double sensorHeight;
    private final double sensorResolution;
    private final double focalLength;
    private final double overlap;
    private final double sidelap;
    private final boolean isInLandscapeOrientation;

    public CameraDetail() {
        name = "Canon SX260";
        sensorWidth = 6.12;
        sensorHeight = 4.22;
        sensorResolution = 12.1;
        focalLength = 5.0;
        overlap = 50.0;
        sidelap = 60.0;
        isInLandscapeOrientation = true;
    }

    public CameraDetail(String name, double sensorWidth, double sensorHeight, double sensorResolution,
                        double focalLength, double overlap, double sidelap,
                        boolean isInLandscapeOrientation) {
        this.name = name;
        this.sensorWidth = sensorWidth;
        this.sensorHeight = sensorHeight;
        this.sensorResolution = sensorResolution;
        this.focalLength = focalLength;
        this.overlap = overlap;
        this.sidelap = sidelap;
        this.isInLandscapeOrientation = isInLandscapeOrientation;
    }

    public CameraDetail(CameraDetail copy) {
        this(copy.name, copy.sensorWidth, copy.sensorHeight, copy.sensorResolution, copy.focalLength, copy.overlap,
                copy.sidelap, copy.isInLandscapeOrientation);
    }

    public String getName() {
        return name;
    }

    public double getSensorWidth() {
        return sensorWidth;
    }

    public double getSensorHeight() {
        return sensorHeight;
    }

    public double getSensorResolution() {
        return sensorResolution;
    }

    public double getFocalLength() {
        return focalLength;
    }

    public double getOverlap() {
        return overlap;
    }

    public double getSidelap() {
        return sidelap;
    }

    public boolean isInLandscapeOrientation() {
        return isInLandscapeOrientation;
    }

    public Double getSensorLateralSize() {
        if (isInLandscapeOrientation) {
            return sensorWidth;
        } else {
            return sensorHeight;
        }
    }

    public Double getSensorLongitudinalSize() {
        if (isInLandscapeOrientation) {
            return sensorHeight;
        } else {
            return sensorWidth;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraDetail)) return false;

        CameraDetail that = (CameraDetail) o;

        if (Double.compare(that.focalLength, focalLength) != 0) return false;
        if (isInLandscapeOrientation != that.isInLandscapeOrientation) return false;
        if (Double.compare(that.overlap, overlap) != 0) return false;
        if (Double.compare(that.sensorHeight, sensorHeight) != 0) return false;
        if (Double.compare(that.sensorResolution, sensorResolution) != 0) return false;
        if (Double.compare(that.sensorWidth, sensorWidth) != 0) return false;
        if (Double.compare(that.sidelap, sidelap) != 0) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        temp = Double.doubleToLongBits(sensorWidth);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(sensorHeight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(sensorResolution);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(focalLength);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(overlap);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(sidelap);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (isInLandscapeOrientation ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CameraDetail{" +
                "name='" + name + '\'' +
                ", sensorWidth=" + sensorWidth +
                ", sensorHeight=" + sensorHeight +
                ", sensorResolution=" + sensorResolution +
                ", focalLength=" + focalLength +
                ", overlap=" + overlap +
                ", sidelap=" + sidelap +
                ", isInLandscapeOrientation=" + isInLandscapeOrientation +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeDouble(this.sensorWidth);
        dest.writeDouble(this.sensorHeight);
        dest.writeDouble(this.sensorResolution);
        dest.writeDouble(this.focalLength);
        dest.writeDouble(this.overlap);
        dest.writeDouble(this.sidelap);
        dest.writeByte(isInLandscapeOrientation ? (byte) 1 : (byte) 0);
    }

    private CameraDetail(Parcel in) {
        this.name = in.readString();
        this.sensorWidth = in.readDouble();
        this.sensorHeight = in.readDouble();
        this.sensorResolution = in.readDouble();
        this.focalLength = in.readDouble();
        this.overlap = in.readDouble();
        this.sidelap = in.readDouble();
        this.isInLandscapeOrientation = in.readByte() != 0;
    }

    public static final Parcelable.Creator<CameraDetail> CREATOR = new Parcelable.Creator<CameraDetail>() {
        public CameraDetail createFromParcel(Parcel source) {
            return new CameraDetail(source);
        }

        public CameraDetail[] newArray(int size) {
            return new CameraDetail[size];
        }
    };
}
