package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.coordinate.LatLong;

/**
 * Stores GPS information.
 */
public class Gps implements DroneAttribute {
    public static final String LOCK_2D = "2D";
    public static final String LOCK_3D = "3D";
    public static final String LOCK_3D_DGPS = "3D+DGPS";
    public static final String LOCK_3D_RTK = "3D+RTK";
    public static final String NO_FIX = "NoFix";

    private final static int LOCK_2D_TYPE = 2;
    private final static int LOCK_3D_TYPE = 3;
    private final static int LOCK_3D_DGPS_TYPE = 4;
    private final static int LOCK_3D_RTK_TYPE = 5;

    private double gpsEph;
    private int satCount;
    private int fixType;
    private LatLong position;

    private boolean vehicleArmed;
    private EkfStatus ekfStatus;

    public Gps() {  }

    public Gps(LatLong position, double gpsEph, int satCount, int fixType){
        this.position = position;
        this.gpsEph = gpsEph;
        this.satCount = satCount;
        this.fixType = fixType;
    }

    public Gps(double latitude, double longitude, double gpsEph, int satCount, int fixType){
        this(new LatLong(latitude, longitude), gpsEph, satCount, fixType);
    }

    public boolean isValid() {
        if (ekfStatus == null) {
            return position != null;
        } else {
            return ekfStatus.isPositionOk(vehicleArmed) && position != null;
        }
    }

    public double getGpsEph(){
        return gpsEph;
    }

    public int getSatellitesCount(){
        return satCount;
    }

    public int getFixType() {
        return fixType;
    }

    public String getFixStatus(){
        switch (fixType) {
            case LOCK_2D_TYPE:
                return LOCK_2D;

            case LOCK_3D_TYPE:
                return LOCK_3D;

            case LOCK_3D_DGPS_TYPE:
                return LOCK_3D_DGPS;

            case LOCK_3D_RTK_TYPE:
                return LOCK_3D_RTK;

            default:
                return NO_FIX;
        }
    }

    public LatLong getPosition() {
        if (isValid()) {
            return position;
        } else {
            return null;
        }
    }

    public void setGpsEph(double gpsEph) {
        this.gpsEph = gpsEph;
    }

    public void setSatCount(int satCount) {
        this.satCount = satCount;
    }

    public void setFixType(int fixType) {
        this.fixType = fixType;
    }

    public void setPosition(LatLong position) {
        this.position = position;
    }

    public void setEkfStatus(EkfStatus ekfStatus) {
        this.ekfStatus = ekfStatus;
    }

    public void setVehicleArmed(boolean vehicleArmed) {
        this.vehicleArmed = vehicleArmed;
    }

    /**
     * @return True if there's a 3D GPS lock, false otherwise.
     * @since 2.6.8
     */
    public boolean has3DLock(){
        return (fixType == LOCK_3D_TYPE) ||
            (fixType == LOCK_3D_DGPS_TYPE) ||
            (fixType == LOCK_3D_RTK_TYPE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gps)) return false;

        Gps gps = (Gps) o;

        if (fixType != gps.fixType) return false;
        if (Double.compare(gps.gpsEph, gpsEph) != 0) return false;
        if (satCount != gps.satCount) return false;
        if (position != null ? !position.equals(gps.position) : gps.position != null)
            return false;
        if (vehicleArmed != gps.vehicleArmed) return false;
        if (ekfStatus != null ? !ekfStatus.equals(gps.ekfStatus) : gps.ekfStatus != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(gpsEph);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + satCount;
        result = 31 * result + fixType;
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (vehicleArmed ? 1 : 0);
        result = 31 * result + (ekfStatus != null ? ekfStatus.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Gps{" +
                "gpsEph=" + gpsEph +
                ", satCount=" + satCount +
                ", fixType=" + fixType +
                ", position=" + position +
                ", vehicleArmed=" + vehicleArmed +
                ", ekfStatus=" + ekfStatus +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.gpsEph);
        dest.writeInt(this.satCount);
        dest.writeInt(this.fixType);
        dest.writeParcelable(this.position, 0);
        dest.writeByte((byte) (vehicleArmed ? 1 : 0));
        dest.writeParcelable(this.ekfStatus, 0);
    }

    private Gps(Parcel in) {
        this.gpsEph = in.readDouble();
        this.satCount = in.readInt();
        this.fixType = in.readInt();
        this.position = in.readParcelable(LatLong.class.getClassLoader());
        this.vehicleArmed = in.readByte() != 0;
        this.ekfStatus = in.readParcelable(EkfStatus.class.getClassLoader());
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
