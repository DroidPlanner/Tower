package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;

/**
 * Reports the vehicle vibration levels and accelerometer clipping.
 * Created by Fredia Huya-Kouadio on 9/14/15.
 */
public class Vibration implements DroneAttribute {

    /*
    Vibration levels thresholds:
    - Good <= 30
    - 30 < Warning <= 60
    - 60 < Danger
     */

    /**
     * Vibration levels on the x-axis.
     */
    private float vibrationX;

    /**
     * Vibration levels on the y-axis.
     */
    private float vibrationY;

    /**
     * Vibration levels on the z-axis.
     */
    private float vibrationZ;

    /**
     * First accelerometer clipping count.
     */
    private long firstAccelClipping;

    /**
     * Second accelerometer clipping count.
     */
    private long secondAccelClipping;

    /**
     * Third accelerometer clipping count.
     */
    private long thirdAccelClipping;

    public Vibration(){}

    public Vibration(long firstAccelClipping, long secondAccelClipping, long thirdAccelClipping, float vibrationX, float vibrationY, float vibrationZ) {
        this.firstAccelClipping = firstAccelClipping;
        this.secondAccelClipping = secondAccelClipping;
        this.thirdAccelClipping = thirdAccelClipping;
        this.vibrationX = vibrationX;
        this.vibrationY = vibrationY;
        this.vibrationZ = vibrationZ;
    }

    public long getFirstAccelClipping() {
        return firstAccelClipping;
    }

    public void setFirstAccelClipping(long firstAccelClipping) {
        this.firstAccelClipping = firstAccelClipping;
    }

    public long getSecondAccelClipping() {
        return secondAccelClipping;
    }

    public void setSecondAccelClipping(long secondAccelClipping) {
        this.secondAccelClipping = secondAccelClipping;
    }

    public long getThirdAccelClipping() {
        return thirdAccelClipping;
    }

    public void setThirdAccelClipping(long thirdAccelClipping) {
        this.thirdAccelClipping = thirdAccelClipping;
    }

    public float getVibrationX() {
        return vibrationX;
    }

    public void setVibrationX(float vibrationX) {
        this.vibrationX = vibrationX;
    }

    public float getVibrationY() {
        return vibrationY;
    }

    public void setVibrationY(float vibrationY) {
        this.vibrationY = vibrationY;
    }

    public float getVibrationZ() {
        return vibrationZ;
    }

    public void setVibrationZ(float vibrationZ) {
        this.vibrationZ = vibrationZ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vibration)) return false;

        Vibration vibration = (Vibration) o;

        if (Float.compare(vibration.vibrationX, vibrationX) != 0) return false;
        if (Float.compare(vibration.vibrationY, vibrationY) != 0) return false;
        if (Float.compare(vibration.vibrationZ, vibrationZ) != 0) return false;
        if (firstAccelClipping != vibration.firstAccelClipping) return false;
        if (secondAccelClipping != vibration.secondAccelClipping) return false;
        return thirdAccelClipping == vibration.thirdAccelClipping;

    }

    @Override
    public int hashCode() {
        int result = (vibrationX != +0.0f ? Float.floatToIntBits(vibrationX) : 0);
        result = 31 * result + (vibrationY != +0.0f ? Float.floatToIntBits(vibrationY) : 0);
        result = 31 * result + (vibrationZ != +0.0f ? Float.floatToIntBits(vibrationZ) : 0);
        result = 31 * result + (int) (firstAccelClipping ^ (firstAccelClipping >>> 32));
        result = 31 * result + (int) (secondAccelClipping ^ (secondAccelClipping >>> 32));
        result = 31 * result + (int) (thirdAccelClipping ^ (thirdAccelClipping >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Vibration{" +
                "firstAccelClipping=" + firstAccelClipping +
                ", vibrationX=" + vibrationX +
                ", vibrationY=" + vibrationY +
                ", vibrationZ=" + vibrationZ +
                ", secondAccelClipping=" + secondAccelClipping +
                ", thirdAccelClipping=" + thirdAccelClipping +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.vibrationX);
        dest.writeFloat(this.vibrationY);
        dest.writeFloat(this.vibrationZ);
        dest.writeLong(this.firstAccelClipping);
        dest.writeLong(this.secondAccelClipping);
        dest.writeLong(this.thirdAccelClipping);
    }

    protected Vibration(Parcel in) {
        this.vibrationX = in.readFloat();
        this.vibrationY = in.readFloat();
        this.vibrationZ = in.readFloat();
        this.firstAccelClipping = in.readLong();
        this.secondAccelClipping = in.readLong();
        this.thirdAccelClipping = in.readLong();
    }

    public static final Creator<Vibration> CREATOR = new Creator<Vibration>() {
        public Vibration createFromParcel(Parcel source) {
            return new Vibration(source);
        }

        public Vibration[] newArray(int size) {
            return new Vibration[size];
        }
    };
}
