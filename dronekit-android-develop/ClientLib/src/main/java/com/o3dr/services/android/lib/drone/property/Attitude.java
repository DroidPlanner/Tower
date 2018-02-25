package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 10/28/14.
 */
public class Attitude implements DroneAttribute {

    /**
     * Roll angle (deg, -180..+180)
     */
    private double roll;

    /**
     * Roll angular speed (deg/s)
     */
    private float rollSpeed;

    /**
     * Pitch angle (deg, -180 to 180)
     */
    private  double pitch;

    /**
     * Pitch angular speed (deg / s)
     */
    private float pitchSpeed;

    /**
     * Yaw angle (deg, -180 to 180)
     */
    private  double yaw;

    /**
     * Yaw angular speed (deg/ s)
     */
    private float yawSpeed;

    public Attitude(){}

    public Attitude(double roll, double pitch, double yaw, float rollSpeed, float pitchSpeed, float yawSpeed) {
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
        this.rollSpeed = rollSpeed;
        this.pitchSpeed = pitchSpeed;
        this.yawSpeed = yawSpeed;
    }

    /**
     * Updates the roll angle
     * @param roll Roll angle (deg, -180..+180)
     */
    public void setRoll(double roll) {
        this.roll = roll;
    }

    /**
     * Updates the pitch angle
     * @param pitch Pitch angle (deg, -180..+180)
     */
    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    /**
     * Updates the yaw angle
     * @param yaw Yaw angle (deg, -180..+180)
     */
    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    /**
     * @return Vehicle roll angle (deg, -180..+180)
     */
    public double getRoll() {
        return roll;
    }

    /**
     * @return Vehicle pitch angle (deg, -180 to 180)
     */
    public double getPitch() {
        return pitch;
    }

    /**
     * @return Vehicle yaw angle (deg, -180 to 180)
     */
    public double getYaw() {
        return yaw;
    }

    /**
     * @return Vehicle pitch angular speed (deg / s)
     */
    public float getPitchSpeed() {
        return pitchSpeed;
    }

    /**
     * Updates the pitch angular speed
     * @param pitchSpeed Pitch angular speed (deg/s)
     */
    public void setPitchSpeed(float pitchSpeed) {
        this.pitchSpeed = pitchSpeed;
    }

    /**
     * @return Vehicle roll angular speed (deg/s)
     */
    public float getRollSpeed() {
        return rollSpeed;
    }

    /**
     * Updates the roll angular speed
     * @param rollSpeed Roll angular speed (deg/s)
     */
    public void setRollSpeed(float rollSpeed) {
        this.rollSpeed = rollSpeed;
    }

    /**
     * @return Vehicle yaw angular speed (deg/ s)
     */
    public float getYawSpeed() {
        return yawSpeed;
    }

    /**
     * Updates the yaw angular speed
     * @param yawSpeed Yaw angular speed (deg/s)
     */
    public void setYawSpeed(float yawSpeed) {
        this.yawSpeed = yawSpeed;
    }

    @Override
    public String toString() {
        return "Attitude{" +
                "pitch=" + pitch +
                ", roll=" + roll +
                ", rollSpeed=" + rollSpeed +
                ", pitchSpeed=" + pitchSpeed +
                ", yaw=" + yaw +
                ", yawSpeed=" + yawSpeed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attitude)) return false;

        Attitude attitude = (Attitude) o;

        if (Double.compare(attitude.roll, roll) != 0) return false;
        if (Float.compare(attitude.rollSpeed, rollSpeed) != 0) return false;
        if (Double.compare(attitude.pitch, pitch) != 0) return false;
        if (Float.compare(attitude.pitchSpeed, pitchSpeed) != 0) return false;
        if (Double.compare(attitude.yaw, yaw) != 0) return false;
        return Float.compare(attitude.yawSpeed, yawSpeed) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(roll);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (rollSpeed != +0.0f ? Float.floatToIntBits(rollSpeed) : 0);
        temp = Double.doubleToLongBits(pitch);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (pitchSpeed != +0.0f ? Float.floatToIntBits(pitchSpeed) : 0);
        temp = Double.doubleToLongBits(yaw);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (yawSpeed != +0.0f ? Float.floatToIntBits(yawSpeed) : 0);
        return result;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.roll);
        dest.writeDouble(this.pitch);
        dest.writeDouble(this.yaw);
        dest.writeFloat(this.rollSpeed);
        dest.writeFloat(this.pitchSpeed);
        dest.writeFloat(this.yawSpeed);
    }

    private Attitude(Parcel in) {
        this.roll = in.readDouble();
        this.pitch = in.readDouble();
        this.yaw = in.readDouble();
        this.rollSpeed = in.readFloat();
        this.pitchSpeed = in.readFloat();
        this.yawSpeed = in.readFloat();
    }

    public static final Parcelable.Creator<Attitude> CREATOR = new Parcelable.Creator<Attitude>() {
        public Attitude createFromParcel(Parcel source) {
            return new Attitude(source);
        }

        public Attitude[] newArray(int size) {
            return new Attitude[size];
        }
    };
}
