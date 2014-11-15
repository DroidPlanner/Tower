package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.coordinate.LatLong;

/**
 * Stores GPS information.
 */
public class Gps implements Parcelable {

    public static final String LOCK_2D = "2D";
    public static final String LOCK_3D = "3D";
    public static final String NO_FIX = "NoFix";

    private final static int LOCK_2D_TYPE = 2;
    private final static int LOCK_3D_TYPE = 3;

    private double mGpsEph;
    private int mSatCount;
    private int mFixType;
    private LatLong mPosition;

    public Gps(){}

    public Gps(LatLong position, double gpsEph, int satCount, int fixType){
        mPosition = position;
        mGpsEph = gpsEph;
        mSatCount = satCount;
        mFixType = fixType;
    }

    public Gps(double latitude, double longitude, double gpsEph, int satCount, int fixType){
        this(new LatLong(latitude, longitude), gpsEph, satCount, fixType);
    }

    public boolean isValid(){
        return mPosition != null;
    }

    public double getGpsEph(){
        return mGpsEph;
    }

    public int getSatellitesCount(){
        return mSatCount;
    }

    public int getFixType(){
        return mFixType;
    }

    public String getFixStatus(){
        switch (mFixType) {
            case LOCK_2D_TYPE:
                return LOCK_2D;

            case LOCK_3D_TYPE:
                return LOCK_3D;

            default:
                return NO_FIX;
        }
    }

    public LatLong getPosition(){
        return mPosition;
    }

    public void setGpsEph(double mGpsEph) {
        this.mGpsEph = mGpsEph;
    }

    public void setSatCount(int mSatCount) {
        this.mSatCount = mSatCount;
    }

    public void setFixType(int mFixType) {
        this.mFixType = mFixType;
    }

    public void setPosition(LatLong mPosition) {
        this.mPosition = mPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gps)) return false;

        Gps gps = (Gps) o;

        if (mFixType != gps.mFixType) return false;
        if (Double.compare(gps.mGpsEph, mGpsEph) != 0) return false;
        if (mSatCount != gps.mSatCount) return false;
        if (mPosition != null ? !mPosition.equals(gps.mPosition) : gps.mPosition != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(mGpsEph);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + mSatCount;
        result = 31 * result + mFixType;
        result = 31 * result + (mPosition != null ? mPosition.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Gps{" +
                "mGpsEph=" + mGpsEph +
                ", mSatCount=" + mSatCount +
                ", mFixType=" + mFixType +
                ", mPosition=" + mPosition +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.mGpsEph);
        dest.writeInt(this.mSatCount);
        dest.writeInt(this.mFixType);
        dest.writeParcelable(this.mPosition, 0);
    }

    private Gps(Parcel in) {
        this.mGpsEph = in.readDouble();
        this.mSatCount = in.readInt();
        this.mFixType = in.readInt();
        this.mPosition = in.readParcelable(LatLong.class.getClassLoader());
    }

    public static final Parcelable.Creator<Gps> CREATOR = new Parcelable.Creator<Gps>() {
        public Gps createFromParcel(Parcel source) {
            return new Gps(source);
        }

        public Gps[] newArray(int size) {
            return new Gps[size];
        }
    };
}
