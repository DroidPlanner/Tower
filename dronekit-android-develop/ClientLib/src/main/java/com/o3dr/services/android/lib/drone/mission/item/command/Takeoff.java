package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

/**
 * The vehicle will climb straight up from it’s current location to the altitude specified (in meters).
 * This should be the first command of nearly all missions.
 * If the mission is begun while the copter is already flying, the vehicle will climb straight up to the specified altitude.
 * If the vehicle is already above the specified altitude the takeoff command will be ignored and the mission will move onto the next command immediately.
 *
 * Created by fhuya on 11/6/14.
 */
public class Takeoff extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    private double takeoffAltitude;
    private double takeoffPitch;

    public Takeoff(){
        super(MissionItemType.TAKEOFF);
    }

    public Takeoff(Takeoff copy){
        this();
        takeoffAltitude = copy.takeoffAltitude;
        takeoffPitch = copy.takeoffPitch;
    }

    /**
     * @return take off altitude in meters
     */
    public double getTakeoffAltitude() {
        return takeoffAltitude;
    }

    /**
     * Sets the take off altitude
     * @param takeoffAltitude Altitude value in meters
     */
    public void setTakeoffAltitude(double takeoffAltitude) {
        this.takeoffAltitude = takeoffAltitude;
    }

    public double getTakeoffPitch() {
        return takeoffPitch;
    }

    public void setTakeoffPitch(double takeoffPitch) {
        this.takeoffPitch = takeoffPitch;
    }

    @Override
    public String toString() {
        return "Takeoff{" +
                "takeoffAltitude=" + takeoffAltitude +
                ", takeoffPitch=" + takeoffPitch +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Takeoff)) return false;
        if (!super.equals(o)) return false;

        Takeoff takeoff = (Takeoff) o;

        if (Double.compare(takeoff.takeoffAltitude, takeoffAltitude) != 0) return false;
        return Double.compare(takeoff.takeoffPitch, takeoffPitch) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(takeoffAltitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(takeoffPitch);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.takeoffAltitude);
        dest.writeDouble(this.takeoffPitch);
    }

    private Takeoff(Parcel in) {
        super(in);
        this.takeoffAltitude = in.readDouble();
        this.takeoffPitch = in.readDouble();
    }

    @Override
    public MissionItem clone() {
        return new Takeoff(this);
    }

    public static final Creator<Takeoff> CREATOR = new Creator<Takeoff>() {
        public Takeoff createFromParcel(Parcel source) {
            return new Takeoff(source);
        }

        public Takeoff[] newArray(int size) {
            return new Takeoff[size];
        }
    };
}
