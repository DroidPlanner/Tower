package org.droidplanner.android.lib.parcelables;

import android.os.Parcel;
import android.os.Parcelable;

import org.droidplanner.core.drone.variables.Speed;

/**
 * Parcelable wrapper for a Speed object.
 */
public class ParcelableSpeed implements Parcelable {

    private double verticalSpeed = 0;
    private double groundSpeed = 0;
    private double airSpeed = 0;
    private double targetSpeed = 0;

    public ParcelableSpeed(Speed speed) {
        this.verticalSpeed = speed.getVerticalSpeed().valueInMetersPerSecond();
        this.groundSpeed = speed.getGroundSpeed().valueInMetersPerSecond();
        this.airSpeed = speed.getAirSpeed().valueInMetersPerSecond();
        this.targetSpeed = speed.getTargetSpeed().valueInMetersPerSecond();
    }

    public double getVerticalSpeed() {
        return verticalSpeed;
    }

    public double getGroundSpeed() {
        return groundSpeed;
    }

    public double getAirSpeed() {
        return airSpeed;
    }

    public double getTargetSpeed() {
        return targetSpeed;
    }

    public void setSpeedError(double aspd_error) {
        targetSpeed = aspd_error + airSpeed;
    }

    public void setGroundAndAirSpeeds(double groundSpeed, double airSpeed, double climb) {
        this.groundSpeed = groundSpeed;
        this.airSpeed = airSpeed;
        this.verticalSpeed = climb;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.verticalSpeed);
        dest.writeDouble(this.groundSpeed);
        dest.writeDouble(this.airSpeed);
        dest.writeDouble(this.targetSpeed);
    }

    private ParcelableSpeed(Parcel in) {
        this.verticalSpeed = in.readDouble();
        this.groundSpeed = in.readDouble();
        this.airSpeed = in.readDouble();
        this.targetSpeed = in.readDouble();
    }

    public static final Creator<ParcelableSpeed> CREATOR = new Creator<ParcelableSpeed>() {
        public ParcelableSpeed createFromParcel(Parcel source) {return new ParcelableSpeed(source);}

        public ParcelableSpeed[] newArray(int size) {return new ParcelableSpeed[size];}
    };
}
