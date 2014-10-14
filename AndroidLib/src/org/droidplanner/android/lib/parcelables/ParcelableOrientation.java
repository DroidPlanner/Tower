package org.droidplanner.android.lib.parcelables;

import android.os.Parcel;
import android.os.Parcelable;

import org.droidplanner.core.drone.variables.Orientation;

/**
 * Parcelable wrapper for an Orientation object.
 */
public class ParcelableOrientation implements Parcelable {
    private double roll = 0;
    private double pitch = 0;
    private double yaw = 0;

    public ParcelableOrientation(Orientation orientation){
        roll = orientation.getRoll();
        pitch = orientation.getPitch();
        yaw = orientation.getYaw();
    }

    public double getRoll() {
        return roll;
    }

    public double getPitch() {
        return pitch;
    }

    public double getYaw() {
        return yaw;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.roll);
        dest.writeDouble(this.pitch);
        dest.writeDouble(this.yaw);
    }

    private ParcelableOrientation(Parcel in) {
        this.roll = in.readDouble();
        this.pitch = in.readDouble();
        this.yaw = in.readDouble();
    }

    public static final Creator<ParcelableOrientation> CREATOR = new
            Creator<ParcelableOrientation>() {
        public ParcelableOrientation createFromParcel(Parcel source) {return new ParcelableOrientation(source);}

        public ParcelableOrientation[] newArray(int size) {return new ParcelableOrientation[size];}
    };
}
