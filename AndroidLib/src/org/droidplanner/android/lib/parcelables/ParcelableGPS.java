package org.droidplanner.android.lib.parcelables;

import android.os.Parcel;
import android.os.Parcelable;

import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.helpers.coordinates.Coord2D;

/**
 * Parcelable wrapper for a GPS object.
 */
public class ParcelableGPS implements Parcelable {

    private double gps_eph = -1;
    private int satCount = -1;
    private int fixType = -1;
    private ParcelableCoord2D position;

    public ParcelableGPS(GPS gps){
        gps_eph = gps.getGpsEPH();
        satCount = gps.getSatCount();
        fixType = gps.getFixTypeNumeric();
        position = new ParcelableCoord2D(gps.getPosition());
    }

    public boolean isPositionValid() {
        return position != null && position.getCoord() != null;
    }

    public Coord2D getPosition() {
        if (isPositionValid()) {
            return position.getCoord();
        } else {
            return new Coord2D(0, 0);
        }
    }

    public double getGpsEPH() {
        return gps_eph;
    }

    public int getSatCount() {
        return satCount;
    }

    public String getFixType() {
        String gpsFix = "";
        switch (fixType) {
            case GPS.LOCK_2D:
                gpsFix = ("2D");
                break;
            case GPS.LOCK_3D:
                gpsFix = ("3D");
                break;
            default:
                gpsFix = ("NoFix");
                break;
        }
        return gpsFix;
    }

    public int getFixTypeNumeric() {
        return fixType;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.gps_eph);
        dest.writeInt(this.satCount);
        dest.writeInt(this.fixType);
        dest.writeParcelable(this.position, 0);
    }

    private ParcelableGPS(Parcel in) {
        this.gps_eph = in.readDouble();
        this.satCount = in.readInt();
        this.fixType = in.readInt();
        this.position = in.readParcelable(ParcelableCoord2D.class.getClassLoader());
    }

    public static final Parcelable.Creator<ParcelableGPS> CREATOR = new Parcelable
            .Creator<ParcelableGPS>() {
        public ParcelableGPS createFromParcel(Parcel source) {return new ParcelableGPS(source);}

        public ParcelableGPS[] newArray(int size) {return new ParcelableGPS[size];}
    };
}
